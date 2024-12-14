package akkademy.mailbox

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.apache.pekko.actor.DeadLetter
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.MailboxSelector
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.scaladsl.adapter._

import akkademy.DeadLetterListener

object LimitedCapacityActor {
  def apply(): Behavior[String] = Behaviors.receive {
    (context, message) =>
      context.log.info(s"Received message: $message")
      Thread.sleep(200)
      Behaviors.same
  }
}

object BoundedMailboxConfig {
  val config: Config = ConfigFactory.parseString("""
    pekko.actor.mailbox {
      bounded-mailbox {
        mailbox-type = "org.apache.pekko.dispatch.BoundedMailbox"
        mailbox-capacity = 2
        mailbox-push-timeout-time = 0s
      }
    }
  """)

}

object BoundedMailboxApp extends App {
  ActorSystem(
    Behaviors.setup[String] {
      context =>
        val limitedCapacityActor = context.spawn(
          LimitedCapacityActor(),
          "LimitedCapacityActor",
          MailboxSelector.fromConfig("pekko.actor.mailbox.bounded-mailbox")
        )

        val deadLetterListener = context.spawn(DeadLetterListener(), "deadLetterListener")
        context.system.toClassic.eventStream.subscribe(deadLetterListener.toClassic, classOf[DeadLetter])

        for (j <- 1 to 3) {
          limitedCapacityActor ! s"Message $j"
        }

        Behaviors.empty
    },
    "BoundedMailboxSystem",
    BoundedMailboxConfig.config
  )
}
