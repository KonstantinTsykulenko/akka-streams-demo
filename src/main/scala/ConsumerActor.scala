import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.stream.actor.ActorSubscriberMessage.OnNext
import akka.stream.actor._

class ConsumerActor(target: ActorRef) extends Actor with ActorLogging with ActorSubscriber {
  override protected def requestStrategy: RequestStrategy = new RequestStrategy {
    override def requestDemand(remainingRequested: Int): Int = {
      Math.max(remainingRequested, 10)
    }
  }

  override def receive: Receive = {
    case OnNext(url) =>
      log.info("Got a url {}", url)
      target ! url
  }
}
