package com.karasiq.webmtv.sosach

import com.karasiq.webmtv.sosach.api.Board._
import dispatch._
import spray.json._

import scala.async.Async.{async, await}
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.language.postfixOps
import scala.util.Try

object SosachApi {
  private def boardGet(apiUrl: String)(implicit ec: ExecutionContext): Future[api.Board] = {
    val req = url(apiUrl)
      .addHeader("User-Agent", "Mozilla/5.0 (X11; Linux i686; rv:34.0) Gecko/20100101 Firefox/34.0")
      .addHeader("X-Forwarded-For", "51.161.66.140")

    Http(req OK ((r) ⇒ r.getResponseBody("UTF-8").parseJson.convertTo[api.Board]))
  }

  def board(name: String, pageLimit: Int = 100)(implicit ec: ExecutionContext): Future[Iterator[api.Board]] = {
    val indexPage = boardGet(s"https://2ch.hk/$name/index.json")
    async {
      val head = await(indexPage)
      val tail = (1 to pageLimit).toIterator.map { i ⇒
        val future = boardGet(s"https://2ch.hk/$name/$i.json")
        Try(Await.result(future, 30 seconds)).toOption
      }
      Iterator.single(head) ++ tail.takeWhile(_.nonEmpty).flatten
    }
  }

  def thread(board: String, id: Long)(implicit ec: ExecutionContext): Future[api.Board] = {
    boardGet(s"https://2ch.hk/$board/res/$id.json")
  }
}
