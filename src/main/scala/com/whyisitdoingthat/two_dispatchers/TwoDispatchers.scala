package com.whyisitdoingthat.two_dispatchers

import akka.actor.Props
import com.whyisitdoingthat.{AkkademyApp, LoggingActor}

case object Start


class Actor1 extends LoggingActor {
  def receive = {
    case Start => {
      log.info("Start")
      TwoDispatchers.shutdown()
    }
  }
}


object TwoDispatchers extends AkkademyApp {
  override val confFile: String = "two-dispatchers"

  val actor1 = system.actorOf(Props[Actor1])

  actor1 ! Start
}
