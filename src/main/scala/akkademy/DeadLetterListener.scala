package akkademy

import org.apache.pekko.actor.DeadLetter
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.Behaviors

object DeadLetterListener {
  def apply(): Behavior[DeadLetter] = Behaviors.receive {
    (context, message) =>
      context.log.info(s"Dead letter received: ${message.message}")
      Behaviors.same
  }
}
