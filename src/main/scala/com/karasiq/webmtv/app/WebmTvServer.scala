package com.karasiq.webmtv.app

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.pattern.{ask, AskTimeoutException}
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import com.karasiq.webmtv.app.WebmStoreDispatcher.{RequestWebmList, WebmList}
import upickle.default._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

final class WebmTvServer(store: ActorRef)(implicit as: ActorSystem) {
  import as.dispatcher

  private implicit def defaultMarshaller[T: Writer]: ToEntityMarshaller[T] =
    Marshaller.withFixedContentType(ContentTypes.`application/json`)((value: T) =>
      HttpEntity(ContentTypes.`application/json`, write(value))
    )

  private def videoList(board: Option[String]): Future[Seq[String]] = {
    implicit val timeout = Timeout(2 minutes)

    (store ? RequestWebmList(board)).collect {
      case WebmList(list) => list
    }.recover {
      case _: AskTimeoutException => Nil
    }
  }

  val route =
    get {
      encodeResponse {
        // Request video URLs
        path(Segment / "videos.json") { board =>
          complete(StatusCodes.OK, videoList(Some(board)))
        } ~
          path("videos.json") {
            complete(StatusCodes.OK, videoList(None))
          } ~
          path("[\\w]+".r) { board =>
            redirect(s"/#$board", StatusCodes.Found)
          } ~
          // Index page
          pathEndOrSingleSlash {
            getFromResource("webapp/index.html", ContentTypes.`text/html(UTF-8)`)
          } ~
          // Other resources
          getFromResourceDirectory("webapp")
      }
    }
}
