import akkademy.greeter.Greeter
import akkademy.greeter.Greeter.Greet
import akkademy.greeter.Greeter.Greeted
import org.apache.pekko.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class GreeterSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  "A Greeter" must {
    "reply to greeted" in {
      val replyProbe = createTestProbe[Greeted]()
      val underTest = spawn(Greeter())
      underTest ! Greet("Santa", replyProbe.ref)
      replyProbe.expectMessage(Greeted("Santa", underTest.ref))
    }
  }
}
