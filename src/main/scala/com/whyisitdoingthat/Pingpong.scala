package com.whyisitdoingthat

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.Logging

class MyActor extends Actor {
  val log = Logging(context.system, this)

  def receive = {
    case s @ "test" => {
      println(s"Received [$s]")
    }
    case _ => {
      println("Received something else")
      Util.caretaker ! Done
    }
  }
}

object Pingpong extends App {
  val actor1 = Util.system.actorOf(Props[MyActor])
  actor1 ! "test"
  actor1 ! 1
}
