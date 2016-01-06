package com.karasiq.webmtv.frontend.app

import com.karasiq.webmtv.frontend.utils.{AppStorage, RxLocation}
import rx._
import upickle.default._

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.Random

trait WebmTvController { self: AppStorage ⇒
  protected val board: Rx[Option[String]] = Rx {
    val hash = RxLocation.location().hash
    if (js.isUndefined(hash) || hash.eq(null) || hash.length <= 1) {
      None
    } else {
      Some(hash.tail)
    }
  }

  protected val videos = Var(Seq.empty[String], "video-list")

  protected val seen = Var({
    val list = load[Seq[String]]("videos-seen", Nil)
    if (list.length > 1000) list.takeRight(1000) else list
  }, "videos-seen")

  protected val videoSource = Rx {
    val sn = seen()
    videos().find(url ⇒ !sn.contains(url))
  }

  protected val loop = Var(false)

  Obs(seen, "videos-seen-ls-writer", skipInitial = true) {
    save("videos-seen", seen())
  }

  Obs(videoSource, "video-list-updater", skipInitial = true) {
    if (videoSource().isEmpty) {
      updateVideos()
    }
  }

  Obs(board, "video-source-changer") {
    updateVideos()
  }

  def updateVideos(): Future[Seq[String]] = {
    val future = WebmTvApi.getVideos(board()).map(list ⇒ Random.shuffle(list))
    future.foreach(list ⇒ videos.update(list))
    future
  }
}
