import akkademy.mailbox.BlockingBoundedMailboxConfig
import akkademy.mailbox.BlockingLimitedCapacityActor
import org.apache.pekko.actor.DeadLetter
import org.apache.pekko.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.apache.pekko.actor.typed.MailboxSelector
import org.apache.pekko.actor.typed.eventstream.EventStream
import org.apache.pekko.actor.typed.scaladsl.adapter._
import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.matchers.should.Matchers


class BlockingBoundedMailboxTest
  extends ScalaTestWithActorTestKit(BlockingBoundedMailboxConfig.config)
  with AnyFunSuiteLike
    with Matchers {

  test("BlockingBoundedMailboxApp should receive no dead letters") {
    val deadLetterProbe = createTestProbe[DeadLetter]()
    val blockingLimitedCapacityActor = spawn(
      BlockingLimitedCapacityActor(),
      "BlockingLimitedCapacityActor",
      MailboxSelector.fromConfig("pekko.actor.mailbox.blocking-bounded-mailbox")
    )

    system.eventStream ! EventStream.Subscribe(deadLetterProbe.ref)
    system.toClassic.eventStream.subscribe(deadLetterProbe.ref.toClassic, classOf[DeadLetter])

    for (j <- 1 to 4) {
      blockingLimitedCapacityActor ! s"Message $j"
    }

    deadLetterProbe.expectNoMessage()
  }
}
