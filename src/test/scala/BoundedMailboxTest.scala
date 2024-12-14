import akkademy.mailbox.LimitedCapacityActor
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.apache.pekko.actor.DeadLetter
import org.apache.pekko.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.apache.pekko.actor.typed.MailboxSelector
import org.apache.pekko.actor.typed.eventstream.EventStream
import org.apache.pekko.actor.typed.scaladsl.adapter._
import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.matchers.should.Matchers


object BoundedMailboxTest {
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

class BoundedMailboxTest
  extends ScalaTestWithActorTestKit(BoundedMailboxTest.config)
  with AnyFunSuiteLike
    with Matchers {

  test("BoundedMailboxApp should receive one dead letter", Focused) {
    val deadLetterProbe = createTestProbe[DeadLetter]()
    val limitedCapacityActor = spawn(
      LimitedCapacityActor(),
      "LimitedCapacityActor",
      MailboxSelector.fromConfig("pekko.actor.mailbox.bounded-mailbox")
    )

    system.eventStream ! EventStream.Subscribe(deadLetterProbe.ref)
    system.toClassic.eventStream.subscribe(deadLetterProbe.ref.toClassic, classOf[DeadLetter])

    for (j <- 1 to 8) {
      limitedCapacityActor ! s"Message $j"
    }

    deadLetterProbe.expectMessageType[DeadLetter]
  }
}
