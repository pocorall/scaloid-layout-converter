package org.scaloid.layout.web

import akka.actor.{ ActorSystem, ActorLogging }
import spray.routing.HttpServiceActor
import org.scaloid.layout.converter._


class Web extends HttpServiceActor with Views with ActorLogging {

  def receive = runRoute {
    path(Slash) {
      get {
        complete(index())
      } ~
      post {
        formField('source) { source =>
          complete {
            index(Some(source), Some(Converter(source)))
          }
        }
      }
    }
  }

  override def preStart: Unit = {
    log.info("Warming up to make the first call faster...")
    Converter {
      <LinearLayout>
        <Button android:text="@string/hello_world"/>
      </LinearLayout>.toString
    }
    log.info("Warmup finished.")
  }


}
