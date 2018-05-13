package com.whyisitdoingthat.same_exec_context

import akka.actor.Props
import com.whyisitdoingthat.{AkkademyApp, LoggingActor}

import scala.concurrent.{ExecutionContext, Future}

case class SlowProcess(idx: Int)


/**
  * The Futures in "actor1" use the same execution context, and will create a bottleneck
  * for receiving messages.
  *
  * You will see the messages arriving in batches, and not in a firehose fashion.
  */
class ActorWithSameExecContext extends LoggingActor {
  def receive = {
    case msg: SlowProcess => {
      // Dispatcher class is also a scala.concurrent.ExecutionContext
      implicit val ex: ExecutionContext = context.dispatcher
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
  override val confFile: String = "two-dispatchers"

  val actor1 = system.actorOf(Props[ActorWithSameExecContext])

  // Messages will not be queued until threads unlock
  for (idx <- 1 to 100) {
    actor1 ! SlowProcess(idx)
  }
}
