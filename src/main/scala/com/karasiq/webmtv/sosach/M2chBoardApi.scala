package com.karasiq.webmtv.sosach

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.karasiq.webmtv.sosach.Board.Thread
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.collection.JavaConversions._

class M2chBoardApi(implicit as: ActorSystem, am: ActorMaterializer) extends BoardApi {
  private val http = Http()

  private def htmlPage(url: String): Source[Document, NotUsed] = {
    Source
      .fromFuture(http.singleRequest(HttpRequest(uri = url)))
      .log("m2ch-html-api")
      .filter(_.status.isSuccess())
      .flatMapConcat(_.entity.dataBytes)
      .fold(ByteString.empty)(_ ++ _)
      .map(bs ⇒ Jsoup.parse(bs.utf8String, url))
  }

  private def replaceDomain(url: String): String = {
    url
    // url.replaceAll("https?://2ch\\.hk", "http://m2-ch.ru")
  }

  override def board(name: String): Source[Thread, NotUsed] = {
    htmlPage(s"http://m2-ch.ru/$name/")
      .flatMapConcat { page ⇒
        val pages = page.select("a.fl_l").map(_.absUrl("href")).toVector
        Source.single(page).concat(Source(pages).flatMapConcat(htmlPage))
      }
      .mapConcat { page ⇒
        val threads = page.select("div.thread.hand")
        threads.toVector.map { thread ⇒
          val id = thread.parent().id().drop(1).toLong
          val title = thread.select("a.oz").first().text()
          val content = thread.select("div.op").html()
          val picture = thread.select("a.il").map(a ⇒ replaceDomain(a.absUrl("href")))
          Board.Thread(name, Seq(Board.Post(id, title, content, picture)))
        }
      }
  }

  override def thread(board: String, id: Long): Source[Thread, NotUsed] = {
    htmlPage(s"http://m2-ch.ru/$board/res/$id.html")
      .map { page ⇒
        val posts = page.select("div.thread, div.reply").map { post ⇒
          val id = post.id().toLong
          val title = post.select("div.pst_bar .ft").headOption.fold("")(_.text())
          val content = post.select("div.pst").headOption.fold("")(_.html())
          val files = post.select("a.thrd-thumb").map(a ⇒ replaceDomain(a.absUrl("href")))
          Board.Post(id, title, content, files)
        }
        Board.Thread(board, posts)
      }
  }
}
