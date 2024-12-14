package akkademy.fault

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.SupervisorStrategy
import org.apache.pekko.actor.typed.scaladsl.Behaviors

object ChildRestartOnErrorApp extends App {
  object ChildActor {
    sealed trait ChildCommand
    case object ThrowException extends ChildCommand

    def apply(): Behavior[ChildCommand] = {
      Behaviors.receiveMessage {
        case ThrowException =>
          throw new RuntimeException("Boom! Child actor failed!")
      }
    }
  }

  object ParentActor {
    sealed trait ParentCommand
    case object TriggerChild extends ParentCommand

    def apply(): Behavior[ParentCommand] = Behaviors.setup {
      context =>
        val child = context.spawn(
          Behaviors
            .supervise(ChildActor())
            .onFailure[RuntimeException](SupervisorStrategy.restart),
          "child-actor"
        )

        Behaviors.receiveMessage {
          case TriggerChild =>
            child ! ChildActor.ThrowException
            Behaviors.same
        }
    }
  }

  val system: ActorSystem[ParentActor.ParentCommand] = ActorSystem(ParentActor(), "ChildStopOnErrorSystem")

  system ! ParentActor.TriggerChild
  system ! ParentActor.TriggerChild
  system ! ParentActor.TriggerChild

  Thread.sleep(1000)
  system.terminate()
}
