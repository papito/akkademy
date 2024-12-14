import akkademy.greeter.Greeter
import akkademy.greeter.Greeter.Greet
import akkademy.greeter.Greeter.Greeted
import org.apache.pekko.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.matchers.should.Matchers

class GreeterTest extends ScalaTestWithActorTestKit with AnyFunSuiteLike with Matchers {

  test("A Greeter should reply to greeted") {
    val replyProbe = createTestProbe[Greeted]()
    val underTest = spawn(Greeter())
    underTest ! Greet("Santa", replyProbe.ref)
    replyProbe.expectMessage(Greeted("Santa", underTest.ref))
  }
}
