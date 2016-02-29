package com.karasiq.webmtv.app

import akka.actor.{Actor, ActorLogging, Props}
import com.karasiq.webmtv.sosach.SosachApi
import com.karasiq.webmtv.sosach.api.Thread
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConversions._

case class ScanBoard(board: String)
case class ScanThread(board: String, id: Long)

case class RequestWebmList(board: Option[String] = None)
case class WebmList(videos: Seq[String])

class WebmThreadScanner(store: WebmStore) extends Actor with ActorLogging {
  import context.dispatcher

  private val config = ConfigFactory.load().getConfig("webm-tv.sosach")

  private def threadFilter(thread: Thread): Boolean = {
    val includes = config.getStringList("include-threads")
    val excludes = config.getStringList("exclude-threads")
    ((includes.isEmpty && thread.posts.flatMap(_.files).exists(_.name.endsWith(".webm"))) || includes.exists(pt ⇒ pt.r.findFirstIn(thread.opPost.comment.toLowerCase).isDefined)) &&
      excludes.forall(pt ⇒ pt.r.findFirstIn(thread.opPost.comment.toLowerCase).isEmpty)
  }

  override def receive: Actor.Receive = {
    case ScanBoard(board) ⇒
      log.info("Rescanning board: /{}/", board)
      val self = context.self
      val sender = context.sender()
      SosachApi.board(board).foreach(_.foreach { page ⇒
        for (thread ← page.threads if threadFilter(thread)) yield {
          self.tell(ScanThread(board, thread.id), sender)
        }
      })

    case ScanThread(board, id) ⇒
      val sender = context.sender()
      val threadId = ThreadId(board, id)
      val cached = store.get(threadId)
      if (cached.isEmpty) {
        log.info("Rescanning thread: /{}/{}", board, id)
        SosachApi.thread(board, id).foreach { thread ⇒
          val files = for (post ← thread.threads.head.posts; file ← post.files if file.name.endsWith(".webm")) yield {
            s"https://2ch.hk/$board/${file.path}"
          }
          store.update(threadId, files)
          sender ! WebmList(files)
        }
      }
  }
}

class WebmStoreDispatcher(store: WebmStore) extends Actor with ActorLogging {
  private val config = ConfigFactory.load().getConfig("webm-tv.sosach")

  private val boards = config.getStringList("boards").toSeq

  private val threadScanner = context.actorOf(Props(classOf[WebmThreadScanner], store), "threadScanner")

  override def receive: Receive = {
    case RequestWebmList(Some(board)) ⇒
      val videos = store.iterator.collect {
        case (ThreadId(`board`, _), files) ⇒
          files
      }.flatten

      if (videos.isEmpty) {
        threadScanner.tell(ScanBoard(board), sender())
        log.info("Awaiting videos for /{}/", board)
      } else {
        sender() ! WebmList(videos.toVector)
      }

    case RequestWebmList(None) ⇒
      val videos = store.iterator.collect {
        case (ThreadId(b, _), files) if boards.contains(b) ⇒
          files
      }.flatten

      if (videos.isEmpty) {
        boards.foreach(board ⇒ threadScanner.tell(ScanBoard(board), sender()))
        log.info("Awaiting videos")
      } else {
        sender() ! WebmList(videos.toVector)
      }
  }
}
