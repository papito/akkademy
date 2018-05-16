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
package com.whyisitdoingthat.pingpong

import akka.actor.{Actor, Props}
import akka.event.Logging
import com.whyisitdoingthat.{AkkademyApp, LoggingActor}

case object Play
case object Ping
case object Pong

class Actor1 extends LoggingActor {
  def receive: Actor.Receive = {
    case Play => {
      log.info("Play")
      Pingpong.actor2 ! Ping
    }
    case Pong => {
      log.info("Pong")
      Pingpong.shutdown()
    }
  }
}

class Actor2 extends LoggingActor {
  def receive: Actor.Receive = {
    case Ping => {
      log.info("Ping")
      sender ! Pong
    }
  }
}

object Pingpong extends AkkademyApp {
  override val confFile: String = "empty"
  override val iterations: Int = 0

  val actor1 = system.actorOf(Props[Actor1])
  lazy val actor2 = system.actorOf(Props[Actor2])

  actor1 ! Play
}
