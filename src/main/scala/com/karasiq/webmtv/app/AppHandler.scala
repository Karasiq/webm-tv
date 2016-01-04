package com.karasiq.webmtv.app

import akka.actor.{Actor, ActorRef, ActorRefFactory}
import akka.pattern.ask
import akka.util.Timeout
import spray.http.MediaTypes
import spray.httpx.SprayJsonSupport._
import spray.httpx._
import spray.json.DefaultJsonProtocol._
import spray.routing.HttpService
import spray.routing.directives.CachingDirectives

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

final class AppHandler(store: ActorRef) extends Actor with HttpService with CachingDirectives {
  import context.dispatcher

  private implicit val timeout = Timeout(3 minutes)

  override def receive: Actor.Receive = runRoute {
    get {
      // Request video URLs
      path(Segment / "videos.json") { board ⇒
        complete((store ? RequestWebmList(Some(board))).collect {
          case WebmList(list) ⇒
            Random.shuffle(list)
        })
      } ~
      path("videos.json") {
        complete((store ? RequestWebmList()).collect {
          case WebmList(list) ⇒
            Random.shuffle(list)
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