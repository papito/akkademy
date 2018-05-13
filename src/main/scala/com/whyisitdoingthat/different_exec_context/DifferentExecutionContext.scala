/* Copyright (c) 2018 Andrei Taranchenko
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.whyisitdoingthat.different_exec_context

import akka.actor.{Actor, Props}
import com.whyisitdoingthat.{AkkademyApp, LoggingActor}
import scala.concurrent.{ExecutionContext, Future}

case class SlowProcess(idx: Int)

class ActorWithDiffExecContext extends LoggingActor {
  def receive: Actor.Receive = {
    case msg: SlowProcess => {
      // Dispatcher class is also a scala.concurrent.ExecutionContext
      implicit val ex: ExecutionContext = context.system.dispatchers.lookup("blocking-dispatcher")
      log.info(s"Received message for ${msg.idx}")

      Future {
        Thread.sleep(800)
        log.info(s"Processing ${msg.idx}")
        DifferentExecutionContext.tick()
      }
    }
  }
}

object DifferentExecutionContext extends AkkademyApp {
  override val confFile: String = "different-exec-context"
  override val iterations: Int = 100

  val actor1 = system.actorOf(Props[ActorWithDiffExecContext])

  // Messages will queue up an be processed in blocking fashion, 4 at a time
  for (idx <- 1 to iterations) {
    actor1 ! SlowProcess(idx)
  }
}
