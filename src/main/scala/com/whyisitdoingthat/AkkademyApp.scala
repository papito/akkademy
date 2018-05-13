package com.whyisitdoingthat

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

  def shutdown(): Unit = {
    println("-- shutting down")
    system.terminate()
  }
}
