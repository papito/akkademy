package akkademy.iot

import org.apache.pekko

import pekko.actor.typed.{ActorSystem, Behavior, PostStop, Signal}
import pekko.actor.typed.scaladsl.AbstractBehavior
import pekko.actor.typed.scaladsl.ActorContext
import pekko.actor.typed.scaladsl.Behaviors

object IotSupervisor {
  def apply(): Behavior[Nothing] =
    Behaviors.setup[Nothing](context => new IotSupervisor(context))
}

private class IotSupervisor(context: ActorContext[Nothing]) extends AbstractBehavior[Nothing](context) {
  context.log.info("IoT Application started")

  override def onMessage(msg: Nothing): Behavior[Nothing] = {
    // No need to handle any messages
    Behaviors.unhandled
  }

  override def onSignal: PartialFunction[Signal, Behavior[Nothing]] = {
    case PostStop =>
      context.log.info("IoT Application stopped")
      this
  }
}
object IotApp extends App {
  ActorSystem[Nothing](IotSupervisor(), "iot-system")
}
