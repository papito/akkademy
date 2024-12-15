import akkademy.fault.ChildStopOnErrorApp.ParentActor
import akkademy.fault.ChildStopOnErrorApp.ParentActor.TriggerChild
import org.apache.pekko.actor.ActorSelection
import org.apache.pekko.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.apache.pekko.actor.testkit.typed.scaladsl.TestProbe
import org.apache.pekko.actor.typed.scaladsl.adapter._
import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.matchers.should.Matchers

class ChildStopOnErrorTest extends ScalaTestWithActorTestKit with AnyFunSuiteLike with Matchers {

  test("Child actor stops upon exception") {
    val parentActor = spawn(ParentActor())
    val childActor = ActorSelection(parentActor.toClassic, "child-actor").resolveOne().futureValue
    val testProbe = TestProbe()

    parentActor ! TriggerChild

    testProbe.expectTerminated(childActor)
  }
}
