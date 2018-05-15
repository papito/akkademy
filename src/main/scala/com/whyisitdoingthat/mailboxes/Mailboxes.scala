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

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorRef, DeadLetter, PoisonPill, Props}
import akka.routing.FromConfig
import com.whyisitdoingthat.{AkkademyApp, LoggingActor}

class AsyncActor extends LoggingActor {
  def receive: Actor.Receive = {
    case msg: String => {
      log.info(s"Async actor: $msg")
      Mailboxes.tick()
    }
  }
}

class BlockingActor extends LoggingActor {
  def receive: Actor.Receive = {
    case msg: String => {
      log.info(s"Blocking actor: $msg")
      Thread.sleep(250)
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
  override val iterations: Int = 60
  val iterationsPerExample = 20

  val deadLetterWatcher = system.actorOf(
    Props[DeadLetterWatcherActor],
    name = "dead-letter-watcher")

  system.eventStream.subscribe(deadLetterWatcher, classOf[DeadLetter])

  println("=================\nBOUNDED MAILBOX\n=================")
  val boundedMailboxActor = system.actorOf(
    Props[AsyncActor].withMailbox("bounded-mailbox"))

  for (idx <- 1 to iterationsPerExample) {
    boundedMailboxActor ! s"Bounded mailbox: $idx"
  }

  Thread.sleep(1000)

  println("=================\nBLOCKING BOUNDED MAILBOX\n=================")
  val blockingBoundedMailboxActor = system.actorOf(
    Props[BlockingActor].withMailbox("blocking-bounded-mailbox"))

  for (idx <- 1 to iterationsPerExample) {
    blockingBoundedMailboxActor ! s"Blocking bounded mailbox: $idx"
  }

  Thread.sleep(1000)

  println("=================\nBLOCKING BOUNDED MAILBOX WITH THREAD POOL\n=================")
  val blockingRouter: ActorRef = system.actorOf(
    FromConfig.props(Props[BlockingActor]), "blocking-router")

  for (idx <- 1 to iterationsPerExample) {
    blockingRouter ! s"Blocking bounded mailbox with threadpool: $idx"
  }
}
