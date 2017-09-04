package com.karasiq.webmtv.app

import scala.collection.JavaConversions._

import akka.actor.{Actor, ActorLogging, Props}
import akka.stream.ActorMaterializer

import com.karasiq.webmtv.sosach.{Board, BoardApi}

case class ScanBoard(board: String)
case class ScanThread(board: String, id: Long)

case class RequestWebmList(board: Option[String] = None)
case class WebmList(videos: Seq[String])

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

class WebmStoreDispatcher(boardApi: BoardApi, store: WebmStore) extends Actor with ActorLogging {
  private[this] val config = context.system.settings.config.getConfig("webm-tv.sosach")
  private[this] val boards = config.getStringList("boards").toSeq
  private[this] val threadScanner = context.actorOf(Props(classOf[WebmThreadScanner], boardApi, store), "threadScanner")

  override def receive: Receive = {
    case RequestWebmList(Some(board)) ⇒
      val videos = store.collect {
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
