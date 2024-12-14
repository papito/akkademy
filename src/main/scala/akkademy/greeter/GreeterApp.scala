package akkademy.greeter

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.Behaviors

object Greeter {
  final case class Greet(whom: String, replyTo: ActorRef[Greeted])
  final case class Greeted(whom: String, from: ActorRef[Greet])

  def apply(): Behavior[Greet] = Behaviors.receive {
    (context, message) =>
      context.log.info("Hello {}!", message.whom)
      // #greeter-send-messages
      message.replyTo ! Greeted(message.whom, context.self)
      Behaviors.same
  }
}

object GreeterBot {
  def apply(max: Int): Behavior[Greeter.Greeted] = {
    bot(0, max)
  }

  private def bot(greetingCounter: Int, max: Int): Behavior[Greeter.Greeted] =
    Behaviors.receive {
      (context, message) =>
        val n = greetingCounter + 1
        context.log.info("Greeting {} for {}", n, message.whom)
        if (n == max) {
          context.log.info("Greeting limit reached for {}, stopping actor", message.whom)
          Behaviors.stopped
        } else {
          message.from ! Greeter.Greet(message.whom, context.self)
          bot(n, max)
        }
    }
}

object GreeterMain {
  final case class SayHello(name: String)

  def apply(): Behavior[SayHello] =
    Behaviors.setup {
      context =>
        // create-actors
        val greeter = context.spawn(Greeter(), "greeter")

        Behaviors.receiveMessage {
          message =>
            // create-actors
            val replyTo = context.spawn(GreeterBot(max = 3), message.name)
            greeter ! Greeter.Greet(message.name, replyTo)
            Behaviors.same
        }
    }
}

object GreeterApp extends App {
  // actor-system
  private val greeterMain: ActorSystem[GreeterMain.SayHello] = ActorSystem(GreeterMain(), "PekkoQuickstart")

  greeterMain ! GreeterMain.SayHello("Charles")
}
