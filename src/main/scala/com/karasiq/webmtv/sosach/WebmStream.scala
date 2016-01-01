package com.karasiq.webmtv.sosach

import com.karasiq.webmtv.sosach.api.Thread
import com.typesafe.config.ConfigFactory
import spray.caching.LruCache

import scala.collection.JavaConversions._
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.language.postfixOps
import scala.util.{Failure, Random, Success, Try}

object WebmStream {
  private val boardCache = LruCache[Stream[api.Board]](timeToLive = 10 minutes)

  private val threadCache = LruCache[api.Board](timeToLive = 10 minutes)

  private val config = ConfigFactory.load().getConfig("webm-tv.sosach")

  private def threadFilter(thread: Thread): Boolean = {
    val includes = config.getStringList("include-threads")
    val excludes = config.getStringList("exclude-threads")
    ((includes.isEmpty && thread.posts.flatMap(_.files).exists(_.name.endsWith(".webm"))) || includes.exists(pt ⇒ pt.r.findFirstIn(thread.opPost.comment.toLowerCase).isDefined)) &&
      excludes.forall(pt ⇒ pt.r.findFirstIn(thread.opPost.comment.toLowerCase).isEmpty)
  }

  def apply(board: String)(implicit ec: ExecutionContext): Iterator[String] = {
    val boardFuture = boardCache(board, () ⇒ SosachApi.board(board).map(_.toStream))
    val threads = Try(Await.result(boardFuture, 1 minutes)) match {
      case Success(pages) ⇒
        for (page <- pages.iterator; thread <- page.threads if threadFilter(thread)) yield {
          threadCache((board, thread.id), () ⇒ SosachApi.thread(board, thread.id))
        }

      case Failure(exc) ⇒
        Iterator.empty
    }

    threads.flatMap { future ⇒
      // Extract files
      val files: Seq[String] = Try(Await.result(future, 1 minutes)) match {
        case Success(thread) ⇒
          for (post <- thread.threads.head.posts; file <- post.files if file.name.endsWith(".webm")) yield {
            s"https://2ch.hk/$board/${file.path}"
          }

        case Failure(exc) ⇒
          Seq.empty
      }
      // Randomize files
      val limit = config.getInt("thread-files-limit")
      if (limit == 0) {
        Random.shuffle(files)
      } else {
        Random.shuffle(files).take(limit)
      }
    }
  }

  def apply()(implicit ec: ExecutionContext): Iterator[String] = {
    config.getStringList("boards").toIterator.flatMap(this.apply)
  }
}
