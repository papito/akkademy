package streams

import org.apache.pekko.NotUsed
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.scaladsl.Flow
import org.apache.pekko.stream.scaladsl.Sink
import org.apache.pekko.stream.scaladsl.Source

import scala.concurrent.Await
import scala.concurrent.duration._

object SimpleStreamApp extends App {
  implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "SimpleStreamSystem")
  implicit val materializer: Materializer = Materializer(system)

  println("Streams #1")
  private val source: Source[Int, NotUsed] = Source(1 to 25).map(_ * 2)
  private val res1 = source.runForeach(println)

  Await.result(res1, Duration.Inf)

  // as these run in different threads, the order of the output is not guaranteed
  println("Streams #2")
  Source(1 to 6).via(Flow[Int].map(_ * 2)).to(Sink.foreach(println(_))).run()

  system.terminate()
}
