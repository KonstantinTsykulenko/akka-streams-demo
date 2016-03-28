import akka.actor.{Actor, ActorLogging}
import akka.stream.actor.ActorPublisher

import scala.collection.mutable

class ProducerActor extends Actor with ActorLogging with ActorPublisher[Url] {

  val queue: mutable.Queue[Url] = mutable.Queue()
  val visited: mutable.Set[String] = mutable.Set()

  override def receive: Receive = {
    case url: Url =>
      if (!visited(url.url)) {
        visited += url.url
        log.info("Processing {}", url)
        queue.enqueue(url)
        if (isActive && totalDemand > 0) {
          log.info("Dequeue {}", url)
          onNext(queue.dequeue())
        }
      }
      else {
        log.info("Dropping already visited {}", url)
      }
  }
}
