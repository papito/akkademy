package com.whyisitdoingthat.different_exec_context

import akka.actor.Props
import com.whyisitdoingthat.{AkkademyApp, LoggingActor}

import scala.concurrent.{ExecutionContext, Future}

case class SlowProcess(idx: Int)


/**
  * The actor uses a separate dispatcher, allocating executor threads for the blocking
  * operations.
  *
  * You will see the actor getting all the messages virtually at once and then going to work,
  * in batches of 8-ish
  */
class ActorWithDiffExecContext extends LoggingActor {
  def receive = {
    case msg: SlowProcess => {
      // Dispatcher class is also a scala.concurrent.ExecutionContext
      implicit val ex: ExecutionContext = context.system.dispatchers.lookup("blocking-dispatcher")
      log.info(s"Received message for ${msg.idx}")

      Future {
        Thread.sleep(800)
        log.info(s"Processing ${msg.idx}")
        if (msg.idx == 100) {
          TwoDispatchers.shutdown()
        }
      }
    }
  }
}

object TwoDispatchers extends AkkademyApp {
  override val confFile: String = "different-exec-context"

  val actor1 = system.actorOf(Props[ActorWithDiffExecContext])

  // Messages will queue up an be processed in blocking fashion, 4 at a time
  for (idx <- 1 to 100) {
    actor1 ! SlowProcess(idx)
  }
}
