import java.net.URI

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.actor.{ActorPublisher, ActorSubscriber}
import akka.stream.scaladsl.{Sink, Source}
import org.jsoup.Jsoup
import spray.client.pipelining._
import spray.http.{HttpRequest, HttpResponse}

import scala.collection.JavaConversions._
import scala.concurrent.Future
import scala.util.Try

object Main extends App {

  implicit val system = ActorSystem("crawler")
  implicit val materializer = ActorMaterializer()

  import system.dispatcher

  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive

  val producerRef = system.actorOf(Props[ProducerActor])
  val consumerRef = system.actorOf(Props(classOf[ConsumerActor], producerRef))
  val publisher = ActorPublisher[Url](producerRef)
  val subscriber = ActorSubscriber[Url](consumerRef)

  Source.fromPublisher(publisher)
    .map(url => pipeline(Get(url.url)).map((url, _)))
    .mapAsync(4)(identity)
    .map(parseUrls)
    .mapConcat(identity)
    .map(url => Url(url.url, url.depth - 1))
    .filter(_.depth >= 0)
    .filter(_.url.contains("akka"))
    .runWith(Sink.fromSubscriber(subscriber))

  Thread.sleep(1000L)

  producerRef ! Url("https://en.wikipedia.org/wiki/Akka_(toolkit)", 2)

  def parseUrls: ((Url, HttpResponse)) => List[Url] = {
    case (url, resp) => Jsoup.parse(resp.entity.asString).select("a[href]").toList.map(_.attr("href"))
      .map(l => if (l.matches("^[\\/#].*")) url + l else l)
      .filter(l => Try(new URI(l)).isSuccess)
      .map(Url(_, url.depth))
  }
}
