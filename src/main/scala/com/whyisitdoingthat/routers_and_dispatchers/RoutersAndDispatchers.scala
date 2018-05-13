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

package com.whyisitdoingthat.routers_and_dispatchers

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.ask
import akka.routing.FromConfig
import akka.util.Timeout
import com.whyisitdoingthat.{AkkademyApp, LoggingActor}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}


class BlockingActor extends LoggingActor {
  def receive: Actor.Receive = {
    case idx: Int => {
      log.info(s"Blocking actor on $idx")
      Thread.sleep(800)
      RoutersAndDispatchers.tick()
    }
  }
}

class NonBlockingActor extends LoggingActor {
  def receive: Actor.Receive = {
    case s: String => {
      sender ! s"Thank you for sending [$s]"
    }
  }
}

object RoutersAndDispatchers extends AkkademyApp {
  override val confFile: String = "routers-and-dispatchers"
  override val iterations: Int = 200
  implicit val timeout: Timeout = Timeout(1.second)

  val blockingRouter: ActorRef = system.actorOf(
    FromConfig.props(Props[BlockingActor]), "blocking-router")

  val nonBlockingRouter: ActorRef = system.actorOf(
    FromConfig.props(Props[NonBlockingActor]), "non-blocking-router")

  for (idx <- 1 to iterations / 2) {
    blockingRouter ! idx
  }

  for (idx <- 1 to iterations / 2) {
    (nonBlockingRouter ? s"MESSAGE $idx").onComplete {
      case Success(value) => {
        println(value)
        tick()
      }
      case Failure(e) => println(s"Exception! $e")
    }
  }


}
