package com.karasiq.webmtv.app

import java.util.concurrent.TimeUnit

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.Timeout
import com.karasiq.webmtv.sosach.M2chBoardApi
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

object Main extends App {
  def startup(): Unit = {
    implicit val timeout = Timeout(20 seconds)

    implicit val actorSystem = ActorSystem("webm-tv")
    implicit val executionContext = actorSystem.dispatcher
    implicit val actorMaterializer = ActorMaterializer(ActorMaterializerSettings(actorSystem))

    val boardApi = new M2chBoardApi()
    val store = WebmFileStore
    val storeDispatcher = actorSystem.actorOf(Props(classOf[WebmStoreDispatcher], boardApi, store), "storeDispatcher")
    val config = ConfigFactory.load().getConfig("webm-tv")
    val server = new Server(storeDispatcher)

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
