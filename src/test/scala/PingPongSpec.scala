import akkademy.pingpong.Command
import akkademy.pingpong.Ping
import akkademy.pingpong.PingPlayer
import org.apache.pekko.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class PingPongSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "A PingPong actor system" must {
    "ping and get a pong" in {
      val probe = createTestProbe[Command]()
      val pingActor = spawn(PingPlayer(3))

      pingActor ! Ping(probe.ref)
    }

    "ping shuts down after X iterations" in {
      val max = 3
      val probe = createTestProbe[Command]()
      val pingActor = spawn(PingPlayer(max))

      for (_ <- 1 to max) {
        pingActor ! Ping(probe.ref)
      }

      probe.expectTerminated(pingActor)
    }
  }
}
