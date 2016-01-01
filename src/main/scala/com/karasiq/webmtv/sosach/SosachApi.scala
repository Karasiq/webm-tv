package com.karasiq.webmtv.sosach

import com.karasiq.webmtv.sosach.api.Board._
import dispatch._
import spray.json._

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

object SosachApi {
  private def boardGet(apiUrl: String)(implicit ec: ExecutionContext): Future[api.Board] = {
    val req = url(apiUrl)
      .addHeader("User-Agent", "Mozilla/5.0 (X11; Linux i686; rv:34.0) Gecko/20100101 Firefox/34.0")
      .addHeader("X-Forwarded-For", "51.161.66.140")

    Http(req OK ((r) ⇒ r.getResponseBody("UTF-8").parseJson.convertTo[api.Board]))
  }

  def board(name: String, pageLimit: Int = 5)(implicit ec: ExecutionContext): Iterator[Future[api.Board]] = {
    val indexPage = boardGet(s"https://2ch.hk/$name/index.json")
    Iterator.single(indexPage) ++ (1 to pageLimit).toIterator
      .map(i ⇒ boardGet(s"https://2ch.hk/$name/$i.json"))
  }

  def thread(board: String, id: Long)(implicit ec: ExecutionContext): Future[api.Board] = {
    boardGet(s"https://2ch.hk/$board/res/$id.json")
  }
}
