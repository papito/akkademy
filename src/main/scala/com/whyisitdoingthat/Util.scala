package com.whyisitdoingthat

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.event.Logging

case object Done

class Caretaker extends Actor {
  val log = Logging(context.system, this)

  def receive = {
    case Done => {
      println("-- shutting down")
      Util.system.terminate()
    }
    case _ => println("Unknown command")
  }
}


object Util {
  val system = ActorSystem()
  val caretaker: ActorRef = system.actorOf(Props[Caretaker])
}
