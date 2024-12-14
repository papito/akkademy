package akkademy.mailbox

import akkademy.DeadLetterListener
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.apache.pekko.actor.DeadLetter
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.MailboxSelector
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.scaladsl.adapter._

object BlockingLimitedCapacityActor {
  def apply(): Behavior[String] = Behaviors.receive {
    (context, message) =>
      context.log.info(s"Received message: $message")
      Thread.sleep(200)
      Behaviors.same
  }
}

object BlockingBoundedMailboxConfig {
  val config: Config = ConfigFactory.parseString("""
    pekko.actor.mailbox {
      blocking-bounded-mailbox {
        mailbox-type = "org.apache.pekko.dispatch.BoundedMailbox"
        mailbox-capacity = 2
        mailbox-push-timeout-time = 1s
      }
    }
  """)
}

object BlockingBoundedMailboxApp extends App {
  val system: ActorSystem[String] = ActorSystem(
    Behaviors.setup[String] {
      context =>
        val limitedCapacityActor = context.spawn(
          BlockingLimitedCapacityActor(),
          "BlockingLimitedCapacityActor",
          MailboxSelector.fromConfig("pekko.actor.mailbox.blocking-bounded-mailbox")
        )

        val deadLetterListener = context.spawn(DeadLetterListener(), "deadLetterListener")
        context.system.toClassic.eventStream.subscribe(deadLetterListener.toClassic, classOf[DeadLetter])

        for (j <- 1 to 5) {
          limitedCapacityActor ! s"Message $j"
        }

        Behaviors.empty
    },
    "BlockingBoundedMailboxSystem",
    BlockingBoundedMailboxConfig.config
  )

  Thread.sleep(2000)
  system.terminate()
}
