package com.karasiq.webmtv.app

import java.util.concurrent.TimeUnit

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.karasiq.webmtv.sosach.Json2chBoardApi

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

object WebmTvMain extends App {
  def startup(): Unit = {
    implicit val timeout = Timeout(20 seconds)

    implicit val actorSystem = ActorSystem("webm-tv")
    implicit val executionContext = actorSystem.dispatcher
    implicit val materializer = ActorMaterializer()

    val config = actorSystem.settings.config.getConfig("webm-tv")
    val boardApi = new Json2chBoardApi(config.getString("sosach.host"), config.getString("sosach.usercode-auth"))

    val store = WebmHeapStore //WebmFileStore
    val storeDispatcher = actorSystem.actorOf(WebmStoreDispatcher.props(boardApi, store), "storeDispatcher")
    val server = new WebmTvServer(storeDispatcher)

    Http().bindAndHandle(server.route, config.getString("host"), config.getInt("port")).onComplete {
      case Success(ServerBinding(address)) ⇒
        actorSystem.log.info("Webm-TV server listening at {}", address)

      case Failure(exc) ⇒
        actorSystem.log.error(exc, "Port binding failure")
        actorSystem.terminate()
    }

    Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
      override def run(): Unit = {
        actorSystem.log.info("Shutting down Webm-TV server")
        actorSystem.registerOnTermination {
          store.close()
        }
        Await.result(actorSystem.terminate(), FiniteDuration(5, TimeUnit.MINUTES))
      }
    }))
  }

  startup()
}
