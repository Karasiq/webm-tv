package com.karasiq.webmtv.app

import java.util.concurrent.TimeUnit

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import com.karasiq.webmtv.sosach.{BoardApi, Json2chBoardApi, M2chBoardApi}

object Main extends App {
  def startup(): Unit = {
    implicit val timeout = Timeout(20 seconds)

    implicit val actorSystem = ActorSystem("webm-tv")
    implicit val executionContext = actorSystem.dispatcher
    implicit val actorMaterializer = ActorMaterializer()

    val config = ConfigFactory.load().getConfig("webm-tv")
    val boardApi = new BoardApi {
      private def apiForHost(host: String): Option[BoardApi] = host match {
        case "" ⇒
          None

        case "m2-ch.ru" ⇒
          Some(new M2chBoardApi())

        case other ⇒
          Some(new Json2chBoardApi(other))
      }

      private val primary = apiForHost(config.getString("sosach.host"))
        .getOrElse(sys.error("Primary 2ch API host is not defined"))

      private val fallback = apiForHost(config.getString("sosach.fallback-host"))
        .getOrElse {
          actorSystem.log.warning("2ch API fallback host is not defined")
          primary
        }

      def board(name: String) = {
        primary.board(name)
          .recoverWithRetries(1, { case _ ⇒ fallback.board(name) })
      }

      def thread(board: String, id: Long) = {
        primary.thread(board, id)
          .recoverWithRetries(1, { case _ ⇒ fallback.thread(board, id) })
      }
    }

    val store = WebmHeapStore //WebmFileStore
    val storeDispatcher = actorSystem.actorOf(Props(classOf[WebmStoreDispatcher], boardApi, store), "storeDispatcher")
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
