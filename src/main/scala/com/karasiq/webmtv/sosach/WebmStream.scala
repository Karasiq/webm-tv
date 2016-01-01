package com.karasiq.webmtv.sosach

import com.karasiq.webmtv.sosach.api.Thread

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

object WebmStream {
  private def threadFilter(thread: Thread): Boolean = {
    thread.opPost.files.exists(_.name.endsWith(".webm")) &&
      "аним[е|у|э]".r.findFirstIn(thread.opPost.comment.toLowerCase).isEmpty &&
      "фап[- ]тред".r.findFirstIn(thread.opPost.comment.toLowerCase).isEmpty
  }

  def apply(board: String)(implicit ec: ExecutionContext): Iterator[String] = {
    val threads = Try(Await.result(SosachApi.board(board), 1 minutes)) match {
      case Success(pages) ⇒
        for (page <- pages; thread <- page.threads if threadFilter(thread)) yield {
          SosachApi.thread(board, thread.id)
        }

      case Failure(exc) ⇒
        println(exc)
        Iterator.empty
    }

    threads.flatMap { future ⇒
      Try(Await.result(future, 1 minutes)) match {
        case Success(thread) ⇒
          for (post <- thread.threads.head.posts; file <- post.files if file.name.endsWith(".webm")) yield {
            s"https://2ch.hk/$board/${file.path}"
          }

        case Failure(exc) ⇒
          println(exc)
          Seq.empty
      }
    }
  }
}
