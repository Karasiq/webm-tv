package com.karasiq.webmtv.app

import akka.actor.{Actor, ActorRefFactory}
import spray.http.MediaTypes
import spray.routing.HttpService

final class AppHandler extends Actor with HttpService {
  override def receive: Actor.Receive = runRoute {
    get {
      // TODO: videos
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