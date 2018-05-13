package com.whyisitdoingthat

import akka.actor.Actor
import akka.event.Logging

abstract class LoggingActor extends Actor {
  val log = Logging(context.system, this)
}
