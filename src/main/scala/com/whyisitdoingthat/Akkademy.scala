package com.whyisitdoingthat

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

case object Slay


trait Akkademy  {
  val confFile: String
  lazy val system: ActorSystem = ActorSystem("akkademy", ConfigFactory.load(confFile))

  def shutdown(): Unit = {
    system.terminate()
  }
}
