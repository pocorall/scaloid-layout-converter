package org.scaloid.layout

import akka.actor.{ ActorSystem, Props }
import spray.http._
import spray.servlet._
import web.Web

class Boot extends WebBoot {

  val system = ActorSystem("scaloid-layout")

  val serviceActor = system.actorOf(Props[Web])
  
}
