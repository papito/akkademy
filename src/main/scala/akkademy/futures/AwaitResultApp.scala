package akkademy.futures

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.AskPattern._
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.util.Timeout

import scala.concurrent.Await
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Failure
import scala.util.Success

object MultiplierActor {
  final case class Multiply(number: Int, replyTo: ActorRef[Result])
  final case class Result(value: Int)

  def apply(): Behavior[Multiply] = Behaviors.receive {
    (context, message) =>
      val result = message.number * 2
      context.log.info(s"Multiplying ${message.number} by 2 to get $result")
      Thread.sleep(1000)
      message.replyTo ! Result(result)
      Behaviors.same
  }
}

object AwaitResultApp extends App {
  implicit val timeout: Timeout = 3.seconds
  implicit val system: ActorSystem[MultiplierActor.Multiply] = ActorSystem(MultiplierActor(), "AwaitResultSystem")
  implicit val ec: ExecutionContextExecutor = system.executionContext

  private val resultFuture: Future[MultiplierActor.Result] = system.ask(ref => MultiplierActor.Multiply(5, ref))

  resultFuture.onComplete {
    case Success(result) =>
      println(s"Received result: ${result.value}")
      system.terminate()
    case Failure(exception) =>
      println(s"Failed to get result: ${exception.getMessage}")
      system.terminate()
  }

  Await.result(system.whenTerminated, Duration.Inf)
}
