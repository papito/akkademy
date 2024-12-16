package streams

import org.apache.pekko.Done
import org.apache.pekko.NotUsed
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.scaladsl.FlowWithContext
import org.apache.pekko.stream.scaladsl.Sink
import org.apache.pekko.stream.scaladsl.Source

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.Future
import scala.util.Random

case class Request(payload: String)
case class ProcessedRequest(result: String)
case class RequestContext(requestId: String, timestamp: Long)

object FlowWithContextApp extends App {
  implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "FlowWithContextSystem")
  implicit val materializer: Materializer = Materializer(system)
  implicit val ec: ExecutionContextExecutor = system.executionContext

  // Simulate request processing
  private def processRequest(request: Request): Future[ProcessedRequest] = Future {
    Thread.sleep(100) // Simulate some processing time
    ProcessedRequest(s"Processed: ${request.payload}")
  }

  // Create a flow that processes requests while maintaining the context
  private val processingFlow: FlowWithContext[Request, RequestContext, ProcessedRequest, RequestContext, NotUsed] =
    FlowWithContext[Request, RequestContext].mapAsync(3)(request => processRequest(request))

  // Utility function to create request ID
  private def generateRequestId(): String = s"req-${Random.alphanumeric.take(8).mkString}"

  // Create some sample requests with context
  private val requests = List(
    Request("Hello"),
    Request("World"),
    Request("AkkaStreams")
  ).map(req => (req, RequestContext(generateRequestId(), System.currentTimeMillis())))

  // Create the source
  val source: Source[(Request, RequestContext),NotUsed] = Source(requests)

  // Create a sink that logs the results with context
  private val sink = Sink.foreach[(ProcessedRequest, RequestContext)] {
    case (result, context) =>
      println(s"""
                 |Request ID: ${context.requestId}
                 |Timestamp: ${context.timestamp}
                 |Result: ${result.result}
                 |""".stripMargin)
  }

  // Build and run the stream, returning a Future[Done]
  val result: Future[Done] = source
    .via(processingFlow)
    .runWith(sink)

  result.onComplete(_ => system.terminate())
}
