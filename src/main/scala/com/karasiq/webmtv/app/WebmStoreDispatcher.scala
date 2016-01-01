package com.karasiq.webmtv.app

import akka.actor.{Actor, ActorLogging, Stash}
import com.karasiq.webmtv.sosach.SosachApi
import com.karasiq.webmtv.sosach.api.Thread
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConversions._

case class ScanBoard(board: String)
case class ScanThread(board: String, id: Long)

case object RequestVideo
case object VideosUpdated
case class WebmVideo(url: String)

class WebmStoreDispatcher extends Actor with ActorLogging with Stash {
  import context.dispatcher

  private val config = ConfigFactory.load().getConfig("webm-tv.sosach")

  private def threadFilter(thread: Thread): Boolean = {
    val includes = config.getStringList("include-threads")
    val excludes = config.getStringList("exclude-threads")
    ((includes.isEmpty && thread.posts.flatMap(_.files).exists(_.name.endsWith(".webm"))) || includes.exists(pt ⇒ pt.r.findFirstIn(thread.opPost.comment.toLowerCase).isDefined)) &&
      excludes.forall(pt ⇒ pt.r.findFirstIn(thread.opPost.comment.toLowerCase).isEmpty)
  }

  override def receive: Receive = {
    case ScanBoard(board) ⇒
      log.info("Rescanning board: /{}/", board)
      val self = context.self
      SosachApi.board(board).foreach(_.foreach { page ⇒
        for (thread <- page.threads if threadFilter(thread)) yield {
          self ! ScanThread(board, thread.id)
        }
      })

    case ScanThread(board, id) ⇒
      val self = context.self
      val cached = WebmStore.get(board, id)
      if (cached.isEmpty) {
        log.info("Rescanning thread: /{}/{}", board, id)
        SosachApi.thread(board, id).foreach { thread ⇒
          val files = for (post <- thread.threads.head.posts; file <- post.files if file.name.endsWith(".webm")) yield {
            s"https://2ch.hk/$board/${file.path}"
          }
          WebmStore.update(board, id, files)
          self ! VideosUpdated
        }
      }

    case VideosUpdated ⇒
      unstashAll()

    case RequestVideo ⇒
      val next = WebmStore.next()
      if (next.isEmpty) {
        log.info("Awaiting videos")
        stash()
      } else {
        next.foreach { url ⇒
          log.info("Video delivered: {}", url)
          sender() ! WebmVideo(url)
        }
      }
  }
}
