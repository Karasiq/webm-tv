package com.karasiq.webmtv.frontend.app

import org.scalajs.dom.ext.Ajax
import upickle.default._

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js

object WebmTvApi {
  private def ajax[T: Reader](url: String): Future[T] = {
    Ajax.get(url)
      .map(r ⇒ read[T](r.responseText))
  }

  def getVideos(boardId: Option[String] = None): Future[Seq[String]] = boardId match {
    case Some(board) ⇒
      ajax[Seq[String]](s"/$board/videos.json?timestamp=${js.Date.now()}")

    case None ⇒
      ajax[Seq[String]](s"/videos.json?timestamp=${js.Date.now()}")
  }
}
