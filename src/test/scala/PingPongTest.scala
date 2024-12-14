import akkademy.pingpong.Command
import akkademy.pingpong.Ping
import akkademy.pingpong.PingPlayer
import org.apache.pekko.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.matchers.should.Matchers

class PingPongTest extends ScalaTestWithActorTestKit with AnyFunSuiteLike with Matchers {

  test("PingPong actor system should ping and get a pong") {
    val probe = createTestProbe[Command]()
    val pingActor = spawn(PingPlayer(3))

    pingActor ! Ping(probe.ref)
  }

  test("PingPong actor system should shut down after the specified number of iterations") {
    val max = 3
    val probe = createTestProbe[Command]()
    val pingActor = spawn(PingPlayer(max))

    for (_ <- 1 to max) {
      pingActor ! Ping(probe.ref)
    }

    probe.expectTerminated(pingActor)
  }
}
