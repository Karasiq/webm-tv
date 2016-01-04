package com.karasiq.webmtv.app

import akka.actor.{Actor, ActorRef, ActorRefFactory}
import akka.pattern.{AskTimeoutException, ask}
import akka.util.Timeout
import spray.http.MediaTypes
import spray.httpx.SprayJsonSupport._
import spray.httpx._
import spray.json.DefaultJsonProtocol._
import spray.routing.HttpService
import spray.routing.directives.CachingDirectives

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

final class AppHandler(store: ActorRef) extends Actor with HttpService with CachingDirectives {
  import context.dispatcher

  private def videoList(board: Option[String]): Future[Seq[String]] = {
    implicit val timeout = Timeout(10 seconds)

    (store ? RequestWebmList(board)).collect {
      case WebmList(list) ⇒
        Random.shuffle(list)
    }.recover {
      case _: AskTimeoutException ⇒
        Nil
    }
  }

  override def receive: Actor.Receive = runRoute {
    get {
      compressResponse() {
        // Request video URLs
        path(Segment / "videos.json") { board ⇒
          complete(videoList(Some(board)))
        } ~
        path("videos.json") {
          complete(videoList(None))
        } ~
        // Index page
        (pathSingleSlash & respondWithMediaType(MediaTypes.`text/html`)) {
          getFromResource("webapp/index.html")
        } ~
        // Other resources
        getFromResourceDirectory("webapp")
      }
    }
  }

  override def actorRefFactory: ActorRefFactory = context
}