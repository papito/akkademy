package streams

import org.apache.pekko.Done
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.scaladsl.Sink
import org.apache.pekko.stream.scaladsl.Source

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import scala.concurrent.Await
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Random

trait BaseIoApp extends App {
  implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "IoSystem")
  implicit val materializer: Materializer = Materializer(system)
  implicit val ec: ExecutionContextExecutor = system.executionContext

  def threadInfo: String = s"(Thread: ${Thread.currentThread().getName})"

  val tempDir: Path = Files.createTempDirectory("io-backpressure-app")
  println(s"Created temp directory: $tempDir")

  private def randomString(length: Int): String = {
    val chars = " abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    (1 to length).map(_ => chars(Random.nextInt(chars.length))).mkString
  }

  private def writeFile(index: Int): Unit = {
    val filePath = Paths.get(tempDir.toString, s"$index.txt")
    val lines = (1 to 10000).map(_ => randomString(100 + Random.nextInt(100)))
    println(s"Writing file $filePath $threadInfo")
    Files.write(filePath, lines.mkString("\n").getBytes)
  }

  def setUp(): Done = {
    val res: Future[Done] = Source(1 to 200)
      .mapAsync(Runtime.getRuntime.availableProcessors)(i => Future(writeFile(i))(system.executionContext))
      .runWith(Sink.ignore)

    Await.result(res, Duration.Inf)
  }

  def tearDown(): Unit = {
    Files
      .walk(tempDir)
      .sorted(java.util.Comparator.reverseOrder())
      .forEach(Files.delete(_))

    system.terminate()
  }
}
