import akkademy.fault.ChildStopOnErrorApp.ParentActor
import akkademy.fault.ChildStopOnErrorApp.ParentActor.TriggerChild
import org.apache.pekko.actor.DeadLetter
import org.apache.pekko.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.apache.pekko.actor.typed.eventstream.EventStream
import org.apache.pekko.actor.typed.scaladsl.adapter._
import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.matchers.should.Matchers

class ChildStopOnErrorTest extends ScalaTestWithActorTestKit with AnyFunSuiteLike with Matchers {

  test("Child actor stops upon exception") {
    val deadLetterProbe = createTestProbe[DeadLetter]()
    system.eventStream ! EventStream.Subscribe(deadLetterProbe.ref)
    system.toClassic.eventStream.subscribe(deadLetterProbe.ref.toClassic, classOf[DeadLetter])

    val parentActor = spawn(ParentActor())

    parentActor ! TriggerChild
    parentActor ! TriggerChild

    deadLetterProbe.expectMessageType[DeadLetter]
  }
}
