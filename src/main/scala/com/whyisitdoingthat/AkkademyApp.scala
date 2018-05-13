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
package com.whyisitdoingthat

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

abstract class AkkademyApp extends App {
  /**
    * Requires specific conf file for each example.
    * Akka configuration described here:
    * https://doc.akka.io/docs/akka/current/general/configuration.html#configuration
    *
    * Files live in src/main/resources/
    */
  val confFile: String
  lazy val system: ActorSystem = ActorSystem("akkademy", ConfigFactory.load(confFile))

  // total number of expected iterations until we shut down the Actor system
  val iterations: Int

  private val iterationsCounter: AtomicInteger = new AtomicInteger(0)

  def shutdown(): Unit = {
    system.terminate()
  }

  /**
    * Increments the iteration counter, atomically, and shut down the system once the counter hits
    * the iterations target.
    */
  def tick(): Int = {
    val counter = iterationsCounter.incrementAndGet()
    if (counter >= iterations) {
      shutdown()
    }
    counter
  }

  /**
    * Hang in a loop until the ticker reaches the counter.
    * Used in situations where it's not easy to increment the ticker.
    */
  def waitToExit(): Unit = {
    do {Thread.sleep(100)} while (iterationsCounter.get < iterations)
    shutdown()
  }

}
