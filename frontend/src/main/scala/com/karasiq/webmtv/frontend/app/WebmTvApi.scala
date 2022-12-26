package com.karasiq.webmtv.frontend.app

import scala.concurrent.Future
import scala.scalajs.js

import org.scalajs.dom.ext.Ajax

trait WebmTvApi {
  final type VideoList = Seq[String]
  def getVideos(boardId: Option[String] = None): Future[VideoList]
}

class JsonWebmTvApi extends WebmTvApi {
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  import upickle.default._

  private[this] val DefaultTimeout = 5000

  private[this] def doAjaxRequest[T: Reader](url: String): Future[T] =
    Ajax.get(url, timeout = DefaultTimeout).map(r ⇒ read[T](r.responseText))

  def getVideos(boardId: Option[String] = None): Future[Seq[String]] =
    boardId match {
      case Some(board) ⇒ doAjaxRequest[Seq[String]](s"/$board/videos.json?timestamp=${js.Date.now()}")

      case None ⇒ doAjaxRequest[Seq[String]](s"/videos.json?timestamp=${js.Date.now()}")
    }
}

object WebmTvApi extends JsonWebmTvApi
