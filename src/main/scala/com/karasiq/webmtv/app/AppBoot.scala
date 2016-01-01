package com.karasiq.webmtv.app

import java.util.concurrent.TimeUnit

import akka.actor._
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import spray.can.Http

import scala.collection.JavaConversions._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

object AppBoot extends App {
  def startup(): Unit = {
    implicit val timeout = Timeout(20 seconds)

    implicit val actorSystem = ActorSystem("webm-tv")

    val store = actorSystem.actorOf(Props[WebmStoreDispatcher], "storeDispatcher")

    val service = actorSystem.actorOf(Props(classOf[AppHandler], store), "webService")

    // Schedule rescan
    val config = ConfigFactory.load().getConfig("webm-tv")

    val boards = config.getStringList("sosach.boards")

    import actorSystem.dispatcher

    actorSystem.scheduler.schedule(1 second, 5 minutes) {
      boards.foreach(board ⇒ store ! ScanBoard(board))
    }

    IO(Http) ? Http.Bind(service, interface = config.getString("host"), port = config.getInt("port"))

    Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
      override def run(): Unit = {
        Await.result(actorSystem.terminate(), FiniteDuration(5, TimeUnit.MINUTES))
      }
    }))
  }

  startup()
}
