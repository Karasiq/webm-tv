package com.karasiq.webmtv.frontend.app

import scala.concurrent.{Future, Promise}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Random, Success}

import org.scalajs.dom
import rx._
import upickle.default._

import com.karasiq.webmtv.frontend.utils.{AppStorage, RxLocation}

trait WebmTvController { self: AppStorage ⇒
  implicit final val rxContext: Ctx.Owner = Ctx.Owner.Unsafe
  protected final val board: Rx[Option[String]] = RxLocation.hash
  protected val videos = Var(Seq.empty[String])
  protected val loop = Var(false)

  protected val seen = Var {
    val list = load[Seq[String]]("videos-seen", Nil)
    if (list.length > 1000) list.takeRight(1000) else list
  }

  protected val videoSource = Rx {
    val sn = seen()
    videos().find(url ⇒ !sn.contains(url))
  }

  seen.triggerLater(save("videos-seen", seen.now))
  videoSource.triggerLater(if (videoSource.now.isEmpty) updateVideos())
  board.trigger(updateVideos())

  def updateVideos(): Future[Seq[String]] = {
    val promise = Promise[Seq[String]]

    WebmTvApi.getVideos(board.now) onComplete {
      case Success(newVideos) ⇒
        promise.success(Random.shuffle(newVideos))

      case Failure(_) ⇒
        dom.window.setTimeout(() ⇒ promise.completeWith(updateVideos()), 1000)
    }

    val future = promise.future
    future.foreach(videos.update)
    future
  }
}
