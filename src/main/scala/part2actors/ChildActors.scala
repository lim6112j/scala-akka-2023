package part2actors
import akka.actor.{Actor, ActorSystem, Props, ActorRef}
object ChildActors extends App {
  object Parent {
    case class CreateChild(name: String)
    case class TellChild(mesage: String)
  }
  class Parent extends Actor {
    import Parent._
    override def receive: Receive = {
      case CreateChild(name) =>
        println(s"${self.path} creating child")
        val childRef = context.actorOf(Props[Child], name)
        context.become(withChild(childRef))
    }
    def withChild(childRef: ActorRef): Receive = {
      case TellChild(message) => childRef forward message
    }
  }
  class Child extends Actor {
    override def receive: Receive = {
      case message => println(s"${self.path} I got: $message")
    }
  }
  import Parent._
  val system = ActorSystem("ParentChildrenDemo")
  val parent = system.actorOf(Props[Parent], "parent")
  parent ! CreateChild("child1")
  parent ! TellChild("hello")

  /**
    * Actor Selection
  **/
  val childSelection = system.actorSelection("/user/parent/child1")
  childSelection ! "I found you"
  /**
    * never pass mutable state or this reference to child
  **/
  object NativeBankAccount {
    case class Deposit(amount: Int)
    case class Withdraw(amount: Int)
    case object InitializeAccount
  }
  class NativeBankAccount extends Actor {
    import NativeBankAccount._
    import CreditCard._
    var amount = 0
    override def receive: Receive = {
      case InitializeAccount =>
        val creditCardRef = context.actorOf(Props[CreditCard])
        creditCardRef ! AttachToAccount(this)
      case Deposit(amount) => println(s"New balance is $amount")
      case Withdraw(amount) => println(s"Withdrawing amount: $amount")
    }
    def deposit(funds: Int) {
      amount += funds
    }
    def withdraw(funds: Int) {
      amount -= funds
    }
  }
  object CreditCard {
    case class AttachToAccount(bankAccount: NativeBankAccount)
    case object CheckStatus
  }
  class CreditCard extends Actor {
    import CreditCard._
    override def receive: Receive = {
      case AttachToAccount(bankAccount: NativeBankAccount) =>

    }
  }
}
