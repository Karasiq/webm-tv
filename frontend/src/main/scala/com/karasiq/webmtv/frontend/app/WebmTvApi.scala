package com.karasiq.webmtv.frontend.app

import org.scalajs.dom.ext.Ajax
import upickle.default._

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js

object WebmTvApi {
  private def ajax(url: String): Future[String] = {
    Ajax.get(url).map(_.responseText)
  }

  def getVideos(boardId: Option[String] = None): Future[Seq[String]] = boardId match {
    case Some(board) ⇒
      ajax(s"/$board/videos.json?timestamp=${js.Date.now()}")
        .map(response ⇒ read[Seq[String]](response))

    case None ⇒
      ajax(s"/videos.json?timestamp=${js.Date.now()}")
        .map(response ⇒ read[Seq[String]](response))
  }
}
