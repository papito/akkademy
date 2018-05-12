package com.whyisitdoingthat

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.event.Logging

case object Slay


class Slayer extends Actor {
  /*
  Gets the Done message and terminates the actor system.
   */
  val log = Logging(context.system, this)

  def receive = {
    case Slay => {
      log.info("shutting down")
      Util.system.terminate()
    }
  }
}


object Util {
  val system = ActorSystem("akkademy")
  val slayer: ActorRef = system.actorOf(Props[Slayer])
}
