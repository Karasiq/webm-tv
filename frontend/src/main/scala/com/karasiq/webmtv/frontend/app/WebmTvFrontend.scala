package com.karasiq.webmtv.frontend.app

import org.scalajs.dom
import org.scalajs.dom.ext.LocalStorage
import org.scalajs.jquery.jQuery
import rx._
import upickle.default._

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import scalatags.JsDom.all._

trait RxLocation {
  private val lc: Var[dom.Location] = Var(dom.window.location)

  def location: Rx[dom.Location] = lc

  jQuery(() ⇒ {
    jQuery(dom.window).on("hashchange", () ⇒ {
      dom.console.log("New location: " + js.JSON.stringify(dom.window.location))
      lc.update(dom.window.location)
    })
  })
}

object WebmTvFrontend extends JSApp with RxLocation {
  private def readLocalStorage[T: Reader](name: String, default: ⇒ T): T = {
    LocalStorage(name).fold(default)(str ⇒ read[T](str))
  }

  private def writeLocalStorage[T: Writer](name: String, value: T): Unit = {
    LocalStorage.update(name, write(value))
  }

  private val board: Rx[Option[String]] = Rx {
    val hash = location().hash
    if (js.isUndefined(hash) || hash.eq(null) || hash.length <= 1) {
      None
    } else {
      Some(hash.tail)
    }
  }

  private val videos = Var(Seq.empty[String], "video-list")

  private val seen = Var({
    val list = readLocalStorage[Seq[String]]("videos-seen", Nil)
    if (list.length > 100) list.takeRight(100) else list
  }, "videos-seen")

  private val videoSource = Rx {
    val sn = seen()
    videos().find(url ⇒ !sn.contains(url))
  }

  Obs(seen, "videos-seen-ls-writer", skipInitial = true) {
    writeLocalStorage("videos-seen", seen())
  }

  Obs(videoSource, "video-list-updater", skipInitial = true) {
    if (videoSource().isEmpty) {
      updateVideos()
    }
  }

  Obs(board, "video-source-changer", skipInitial = true) {
    updateVideos()
  }

  def updateVideos(): Future[Seq[String]] = {
    val future = WebmTvApi.getVideos(board())
    future.foreach(list ⇒ videos.update(list))
    future
  }

  @JSExport
  override def main(): Unit = {
    jQuery { () ⇒
      updateVideos().foreach { _ ⇒
        val container = jQuery("#main-container")
        container.append {
          WebmTvHtml.videoContainer(videoSource, seen)(id := "webm-tv-video", marginTop := 10.px, width := container.width() - 120).render
        }
      }
    }
  }
}
