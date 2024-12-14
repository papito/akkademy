package akkademy.pingpong

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.Behaviors

sealed trait Command
case class Ping(replyTo: ActorRef[Pong]) extends Command
case class Pong(replyTo: ActorRef[Ping]) extends Command

object PingPlayer {
  private var count = 0

  def apply(max: Int): Behavior[Ping] = Behaviors.receive {
    (context, message) =>
      message match {
        case Ping(replyTo) =>
          count = count + 1
          context.log.info("{} received Ping,  count: {}", context.self.path, count)

          if (count >= max) {
            Behaviors.stopped
          } else {
            replyTo ! Pong(context.self)
            Behaviors.same
          }
      }
  }
}

object PongPlayer {
  def apply(): Behavior[Pong] = Behaviors.receive {
    (context, message) =>
      message match {
        case Pong(replyTo) =>
          context.log.info("{} received Pong", context.self.path)
          replyTo ! Ping(replyTo = context.self)
          Behaviors.same
      }
  }
}

object PingPongApp extends App {
  val system: ActorSystem[Ping] = ActorSystem(
    Behaviors.setup[Ping] {
      context =>
        val player1 = context.spawn(PingPlayer(3), "playerPong")
        val player2 = context.spawn(PongPlayer(), "playerPing")
        player1 ! Ping(replyTo = player2)
        Behaviors.same
    },
    "PingPongSystem"
  )

  Thread.sleep(1000)
  system.terminate()

}
