package com.whyisitdoingthat

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.Logging

case object Play
case object Ping
case object Pong

class Actor1 extends Actor {
  val log = Logging(context.system, this)

  def receive = {
    case Play => {
      println("Play")
      Pingpong.actor2 ! Ping
    }
    case Pong => {
      println("Pong")
      Util.caretaker ! Done
    }
  }
}

class Actor2 extends Actor {
  val log = Logging(context.system, this)

  def receive = {
    case Ping => {
      println("Ping")
      sender ! Pong
    }
  }
}

object Pingpong extends App {
  val actor1 = Util.system.actorOf(Props[Actor1])
  lazy val actor2 = Util.system.actorOf(Props[Actor2])

  actor1 ! Play
}
