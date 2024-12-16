package streams

import org.apache.pekko.stream.scaladsl.FileIO
import org.apache.pekko.stream.scaladsl.Framing
import org.apache.pekko.stream.scaladsl.Sink
import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.util.ByteString

import java.nio.file.Files
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.jdk.CollectionConverters.IteratorHasAsScala

object SequentialIoApp extends BaseIoApp {
  setUp()

  private val fileSource = Source.fromIterator(() => Files.list(tempDir).iterator().asScala)

  private val res = fileSource
    .flatMapConcat {
      path =>
        Source.future(
          FileIO
            .fromPath(path)
            .via(Framing.delimiter(ByteString("\n"), 256, allowTruncation = true))
            .map(_.utf8String)
            .mapConcat(_.split(" ").toList)
            .fold(0)((count, _) => count + 1)
            .runWith(Sink.head)
            .map(count => s"${path.getFileName}: $count words $threadInfo")
        )
    }
    .runWith(Sink.foreach(println))

  Await.result(res, Duration.Inf)

  tearDown()
}
