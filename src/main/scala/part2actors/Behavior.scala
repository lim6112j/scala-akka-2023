package part2actors

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import part2actors.ChangingActorBehavior.Mom.MomStart
import scala.collection.mutable


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
  // execise 1
  // counter app with context.become and no mutable state


  object Counter {
    case object Increment
    case object Decrement
    case object Print
  }

  class Counter extends Actor {
    import Counter._
    override def receive: Receive = countReceive(0)
    def countReceive(count: Int): Receive = {
      case Increment => context.become(countReceive(count + 1), true)
      case Decrement => context.become(countReceive(count - 1), true)
      case Print => println(s"Current value: ${count}")
    }

  }

  import Counter._
  val counter = system.actorOf(Props[Counter], "myCounter")
  (1 to 5).foreach(_ => counter ! Increment)
  (1 to 3).foreach(_ => counter ! Decrement)
  counter ! Print

  // excercise 2

  case class Vote(candidate: String)
  case object VoteStatusRequest
  case class VoteStatusReply(candidate: Option[String])
  var votesStatus : mutable.Map[ActorRef, String] = mutable.Map()

  class Citizen extends Actor {
    override def receive: Receive = {
      case Vote(s) => votesStatus += (self -> s)
      case VoteStatusRequest =>
//        println("votesStatus size :" + votesStatus.size + ",fsender : " + self.toString())
        votesStatus contains self match {
          case true => sender() ! VoteStatusReply(Some(votesStatus(self)))
          case false => sender() ! VoteStatusReply(None)
        }
    }
  }
  case class AgregateVotes(citizens: Set[ActorRef])

  class VoteAgregattor extends Actor {
    var candidateMap : mutable.Map[String, Int] = mutable.Map()
    override def receive: Receive = {
      case VoteStatusReply(candidate) =>
        candidate match {
          case Some(s) =>
            candidateMap contains s match {
              case true =>
                candidateMap += (s -> (candidateMap(s) + 1))
                println(candidateMap)
              case false => candidateMap += (s -> 1)
            }
          case None =>
        }
      case AgregateVotes(citizens) =>
        for (citizen <- citizens) citizen ! VoteStatusRequest
    }
  }
  val alice = system.actorOf(Props[Citizen])
  val bob = system.actorOf(Props[Citizen])
  val charlie = system.actorOf(Props[Citizen])
  val daniel = system.actorOf(Props[Citizen])
  alice ! Vote("Martin")
  bob ! Vote("Jonas")
  charlie ! Vote("Roland")
  daniel ! Vote("Roland")
  val voteAgregator = system.actorOf(Props[VoteAgregattor])
  voteAgregator ! AgregateVotes(Set(alice, bob, charlie, daniel))

  /*
   print the status of the votes
   Martin -> 1
   Jonas -> 1
   Roland -> 2
   */
}
