package com.karasiq.webmtv.app

import akka.actor.{Actor, ActorRef, ActorRefFactory}
import akka.pattern.ask
import akka.util.Timeout
import spray.http.MediaTypes
import spray.routing.HttpService

import scala.concurrent.duration._
import scala.language.postfixOps

final class AppHandler(store: ActorRef) extends Actor with HttpService {
  import context.dispatcher

  private implicit val timeout = Timeout(3 minutes)

  override def receive: Actor.Receive = runRoute {
    get {
      // Request video URL
      path("video") {
        complete((store ? RequestVideo).collect {
          case WebmVideo(url) ⇒
            url
        })
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