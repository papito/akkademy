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
package com.whyisitdoingthat.handle_actor_response

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.whyisitdoingthat.{AkkademyApp, LoggingActor}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

class Actor1 extends LoggingActor {
  context.setReceiveTimeout(Duration.Inf)
  def receive: Actor.Receive = {
    case number: Int => {
      sender ! number * 10
    }
  }
}

object HandleActorResponse extends AkkademyApp {
  override val confFile: String = "empty"

  val actor1 = system.actorOf(Props[Actor1])
  lazy val responseCount: AtomicInteger = new AtomicInteger(0)

  implicit val timeout: Timeout = Timeout(1.second)

  (actor1 ? 1).onComplete {
    case Success(value) => {
      println(s"Got back [$value]")
      HandleActorResponse.responseCount.incrementAndGet()
    }
    case Failure(e) => println(s"Exception! $e")
  }

  (actor1 ? 2).onComplete {
    case Success(value) => {
      println(s"Got back [$value]")
      HandleActorResponse.responseCount.incrementAndGet()
    }
    case Failure(e) => println(s"Exception! $e")
  }

  (actor1 ? 3).onComplete {
    case Success(value) => {
      println(s"Got back [$value]")
      HandleActorResponse.responseCount.incrementAndGet()
    }
    case Failure(e) => println(s"Exception! $e")
  }
  do {} while (responseCount.get < 3)
  shutdown()
}
