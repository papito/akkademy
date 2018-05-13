package com.whyisitdoingthat

import akka.actor.{Actor, Props}
import akka.event.Logging

case object Play
case object Ping
case object Pong

/*
  Two actors play Ping Pong once
*/

class Actor1 extends Actor {
  val log = Logging(context.system, this)

  def receive = {
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

class Actor2 extends Actor {
  val log = Logging(context.system, this)

  def receive = {
    case Ping => {
      log.info("Ping")
      sender ! Pong
    }
  }
}

object Pingpong extends App with Akkademy {
  override val confFile: String = "pingpong"

  val actor1 = system.actorOf(Props[Actor1])
  lazy val actor2 = system.actorOf(Props[Actor2])

  actor1 ! Play
}
