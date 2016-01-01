package com.karasiq.webmtv.app

import akka.actor.{Actor, ActorRefFactory}
import com.karasiq.webmtv.sosach.WebmStream
import spray.http.MediaTypes
import spray.routing.{Directive1, HttpService}

final class AppHandler extends Actor with HttpService {
  import context.dispatcher

  private var stream: Iterator[String] = Iterator.empty

  private def videoUrl: Directive1[String] = {
    extract { _ ⇒
      if (!stream.hasNext) {
        stream = WebmStream("b")
      }
      stream.next()
    }
  }

  override def receive: Actor.Receive = runRoute {
    get {
      // Request video URL
      (path("video") & videoUrl) { url ⇒
        complete(url)
      } ~
      // Index page
      (pathSingleSlash & respondWithMediaType(MediaTypes.`text/html`)) {
        getFromResource("webapp/index.html")
      } ~
      // Other resources
      getFromResourceDirectory("webapp")
    }
  }

  override def actorRefFactory: ActorRefFactory = context
}