package akkademy.router

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.scaladsl.Routers

object MyWorkerActor {
  def apply(): Behavior[String] = Behaviors.receive {
    (context, message) =>
      println(s"Processing message: $message")
      Thread.sleep(80)
      Behaviors.same
  }
}

object RouterThreadPoolFloodApp extends App {
  // Get the number of available processors
  private val numberOfCores = Runtime.getRuntime.availableProcessors()

  ActorSystem(
    Behaviors.setup[String] {
      context =>
        val pool = Routers.pool(poolSize = numberOfCores)(MyWorkerActor())
        val router = context.spawn(pool, "myRouter")

        for (i <- 1 to 1000) {
          router ! s"Message $i"
        }

        Behaviors.empty
    },
    "RouterMessageBumRush"
  )
}
