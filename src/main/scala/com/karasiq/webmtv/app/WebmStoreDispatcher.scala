package com.karasiq.webmtv.app

import akka.actor.{Actor, ActorLogging, Stash}
import com.karasiq.webmtv.sosach.SosachApi
import com.karasiq.webmtv.sosach.api.Thread
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConversions._

case class ScanBoard(board: String)
case class ScanThread(board: String, id: Long)

case class RequestWebmList(board: Option[String] = None)
case class WebmList(videos: Seq[String])

class WebmStoreDispatcher(store: WebmStore) extends Actor with ActorLogging with Stash {
  private case object VideosUpdated

  import context.dispatcher

  private val config = ConfigFactory.load().getConfig("webm-tv.sosach")

  private val boards = config.getStringList("boards").toSeq

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
      val threadId = ThreadId(board, id)
      val cached = store.get(threadId)
      if (cached.isEmpty) {
        log.info("Rescanning thread: /{}/{}", board, id)
        SosachApi.thread(board, id).foreach { thread ⇒
          val files = for (post <- thread.threads.head.posts; file <- post.files if file.name.endsWith(".webm")) yield {
            s"https://2ch.hk/$board/${file.path}"
          }
          store.update(threadId, files)
          self ! VideosUpdated
        }
      }

    case VideosUpdated ⇒
      unstashAll()

    case RequestWebmList(Some(board)) ⇒
      val videos = store.iterator.collect {
        case (ThreadId(`board`, _), files) ⇒
          files
      }.flatten

      if (videos.isEmpty) {
        self ! ScanBoard(board)
        log.info("Awaiting videos for /{}/", board)
        stash()
      } else {
        sender() ! WebmList(videos.toVector)
      }

    case RequestWebmList(None) ⇒
      val videos = store.iterator.flatMap(_._2)

      if (videos.isEmpty) {
        boards.foreach(board ⇒ self ! ScanBoard(board))
        log.info("Awaiting videos")
        stash()
      } else {
        sender() ! WebmList(videos.toVector)
      }
  }
}
