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

package com.whyisitdoingthat.mailboxes

import akka.actor.{Actor, DeadLetter, Props}
import com.whyisitdoingthat.{AkkademyApp, LoggingActor}

class BoundedMailboxActor extends LoggingActor {
  // Gets an integer and multiplies it by 10
  def receive: Actor.Receive = {
    case number: Int => {
      log.info(s"Got $number")
      // sender ! number * 10
      println("!!!!!!!!!")
      Mailboxes.tick()
    }
  }
}

class DeadLetterWatcherActor extends LoggingActor {
  var deadLetterCount = 0

  def receive: Actor.Receive = {
    case d: DeadLetter => {
      deadLetterCount += 1
      log.error(s"$d. Dead letters now: $deadLetterCount")
      Mailboxes.tick()
    }
    case msg => log.info(s"Dead letter watch: $msg")
  }
}

object Mailboxes extends AkkademyApp {
  override val confFile: String = "mailboxes"
  override val iterations: Int = 20
  val boundedMailboxActor = system.actorOf(
    Props[BoundedMailboxActor].withMailbox("bounded-mailbox"))

  val deadLetterWatcher = system.actorOf(
    Props[DeadLetterWatcherActor],
    name = "dead-letter-watcher")

  system.eventStream.subscribe(deadLetterWatcher, classOf[DeadLetter])

  for (idx <- 1 to iterations) {
    boundedMailboxActor ! idx
  }
}
