package com.karasiq.webmtv.app

import akka.actor.{Actor, ActorLogging, Props}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.karasiq.webmtv.sosach.{Board, BoardApi}
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConversions._

case class ScanBoard(board: String)
case class ScanThread(board: String, id: Long)

case class RequestWebmList(board: Option[String] = None)
case class WebmList(videos: Seq[String])

class WebmThreadScanner(boardApi: BoardApi, store: WebmStore) extends Actor with ActorLogging {
  private implicit val actorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

  private val config = ConfigFactory.load().getConfig("webm-tv.sosach")

  private def threadFilter(thread: Board.Thread): Boolean = {
    val includes = config.getStringList("include-threads")
    val excludes = config.getStringList("exclude-threads")
    ((includes.isEmpty && thread.posts.flatMap(_.files).exists(_.endsWith(".webm"))) || includes.exists(pt ⇒ pt.r.findFirstIn(thread.opPost.content.toLowerCase).isDefined)) &&
      excludes.forall(pt ⇒ pt.r.findFirstIn(thread.opPost.content.toLowerCase).isEmpty)
  }

  override def receive: Actor.Receive = {
    case ScanBoard(board) ⇒
      log.info("Rescanning board: /{}/", board)
      val self = context.self
      val sender = context.sender()
      boardApi.board(board)
        .filter(threadFilter)
        .runForeach { thread ⇒
          self.tell(ScanThread(board, thread.id), sender)
        }

    case ScanThread(board, id) ⇒
      val sender = context.sender()
      val threadId = ThreadId(board, id)
      val cached = store.get(threadId)
      if (cached.isEmpty) {
        log.info("Rescanning thread: /{}/{}", board, id)
        boardApi.thread(board, id).runForeach { thread ⇒
          val files = for (post ← thread.posts; file ← post.files if file.endsWith(".webm"))
            yield file.replaceFirst("^https?://", "//")
          store.update(threadId, files)
          sender ! WebmList(files)
        }
      }
  }
}

class WebmStoreDispatcher(boardApi: BoardApi, store: WebmStore) extends Actor with ActorLogging {
  private val config = ConfigFactory.load().getConfig("webm-tv.sosach")

  private val boards = config.getStringList("boards").toSeq

  private val threadScanner = context.actorOf(Props(classOf[WebmThreadScanner], boardApi, store), "threadScanner")

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
