package part2actors

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import part2actors.ChangingActorBehavior.Mom.MomStart

object ChangingActorBehavior extends App {
  object FussyKid {
    case object KidAccept
    case object KidReject
    val HAPPY = "happy"
    val SAD = "sad"
  }
  class FussyKid extends Actor {
    import FussyKid._
    import Mom._
    // internal state of kid
    var state = HAPPY
    override def receive: Receive = {
      case Food(VEGETABLE) => state = SAD
      case Food(CHOCOLATE) => state = HAPPY
      case Ask(_) =>
        if (state == HAPPY) sender() ! KidAccept
        else sender() ! KidReject
    }
  }
  class StatelessFussyKid extends Actor {
    import FussyKid._
    import Mom._
    override def receive: Receive = happyReceive
    def happyReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceiver, true) // change my receive handler to sadReceiver
      case Food(CHOCOLATE) => // happy receiver
      case Ask(_) => sender() ! KidAccept
    }

    def sadReceiver: Receive = {
      case Food(VEGETABLE) =>
      case Food(CHOCOLATE) => context.become(happyReceive, true)// change my receive handler to happyReceiver
      case Ask(_) => sender() ! KidReject
    }
  }
  object Mom {
    case class Food(food: String)
    case class Ask(message: String)
    case class MomStart(kidRef: ActorRef)
    val VEGETABLE = "vegetable"
    val CHOCOLATE = "chocolate"
  }
  class Mom extends Actor {
    import Mom._
    import FussyKid._
    override def receive: Receive = {
      case MomStart(kidRef) =>
        kidRef ! Food(VEGETABLE)
        kidRef ! Ask("do you want to play?")
        kidRef ! Food(CHOCOLATE)
        kidRef ! Ask("do you want to play?")
      case KidAccept => println ("kid happy")
      case KidReject => println ("kid unhappy")
    }
  }
  val system = ActorSystem("changingActorBehavior")
  val fussyKid = system.actorOf(Props[FussyKid])
  val mom = system.actorOf(Props[Mom])
//  mom ! MomStart(fussyKid)

  val statelessKid = system.actorOf(Props[StatelessFussyKid])
  mom ! Mom.MomStart(statelessKid)
}
