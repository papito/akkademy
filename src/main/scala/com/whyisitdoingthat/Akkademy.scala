package com.whyisitdoingthat

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.event.Logging

case object Slay


trait Akkademy  {
  val system: ActorSystem = ActorSystem("akkademy")

  def shutdown(): Unit = {
    system.terminate()
  }
}
