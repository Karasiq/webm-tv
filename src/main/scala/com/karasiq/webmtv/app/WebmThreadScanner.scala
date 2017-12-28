package com.karasiq.webmtv.app

import scala.collection.JavaConversions._

import akka.actor.{Actor, ActorLogging, Props}
import akka.stream.ActorMaterializer

import com.karasiq.webmtv.app.WebmStoreDispatcher.WebmList
import com.karasiq.webmtv.app.WebmThreadScanner.{ScanBoard, ScanThread}
import com.karasiq.webmtv.sosach.{Board, BoardApi}

object WebmThreadScanner {
  sealed trait Message
  final case class ScanBoard(board: String) extends Message
  final case class ScanThread(board: String, id: Long) extends Message

  def props(boardApi: BoardApi, store: WebmStore): Props = {
    Props(new WebmThreadScanner(boardApi, store))
  }
}

class WebmThreadScanner(boardApi: BoardApi, store: WebmStore) extends Actor with ActorLogging {
  private[this] implicit val actorMaterializer = ActorMaterializer()

  private[this] val config = context.system.settings.config.getConfig("webm-tv.sosach")
  private[this] object settings {
    val videoExtensions: Set[String] = config.getStringList("video-extensions").toSet
    val includes: Seq[String] = config.getStringList("include-threads")
    val excludes: Seq[String] = config.getStringList("exclude-threads")
  }

  private[this] def isVideoFile(file: String): Boolean = {
    file.split("\\.").lastOption.exists(settings.videoExtensions.contains)
  }

  private[this] def isVideoThread(thread: Board.Thread): Boolean = {
    ((settings.includes.isEmpty && thread.posts.flatMap(_.files).exists(isVideoFile)) ||
      settings.includes.exists(pt ⇒ pt.r.findFirstIn(thread.opPost.content.toLowerCase).isDefined)) &&
      settings.excludes.forall(pt ⇒ pt.r.findFirstIn(thread.opPost.content.toLowerCase).isEmpty)
  }

  override def receive: Actor.Receive = {
    case ScanBoard(board) ⇒
      log.info("Rescanning board: /{}/", board)
      val self = context.self
      val sender = context.sender()
      boardApi.board(board)
        .filter(isVideoThread)
        .runForeach(thread ⇒ self.tell(ScanThread(board, thread.id), sender))

    case ScanThread(board, id) ⇒
      val sender = context.sender()
      val threadId = ThreadId(board, id)
      val cached = store.get(threadId)
      if (cached.isEmpty) {
        log.info("Rescanning thread: /{}/{}", board, id)
        boardApi.thread(board, id).runForeach { thread ⇒
          val files = for (post ← thread.posts; file ← post.files if isVideoFile(file))
            yield file.replaceFirst("^https?://", "//")
          store.update(threadId, files)
          sender ! WebmList(files)
        }
      }
  }
}