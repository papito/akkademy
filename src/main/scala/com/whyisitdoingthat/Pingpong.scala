package com.whyisitdoingthat

import akka.actor.{Actor, Props}
import akka.event.Logging

case object Play
case object Ping
case object Pong


class Actor1 extends Actor {
  val log = Logging(context.system, this)

  def receive = {
    case Play => {
      log.info("Play")
      Pingpong.actor2 ! Ping
    }
    case Pong => {
      log.info("Pong")
      Util.slayer ! Slay
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

object Pingpong extends App {
  /* Two actors play Ping Pong once */

  val actor1 = Util.system.actorOf(Props[Actor1])
  lazy val actor2 = Util.system.actorOf(Props[Actor2])

  actor1 ! Play
}
