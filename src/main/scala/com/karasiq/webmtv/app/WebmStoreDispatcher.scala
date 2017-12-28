package com.karasiq.webmtv.app

import scala.collection.JavaConversions._

import akka.actor.{Actor, ActorLogging, Props}

import com.karasiq.webmtv.app.WebmStoreDispatcher.{RequestWebmList, WebmList}
import com.karasiq.webmtv.app.WebmThreadScanner.ScanBoard
import com.karasiq.webmtv.sosach.BoardApi

object WebmStoreDispatcher {
  sealed trait Message
  final case class RequestWebmList(board: Option[String] = None) extends Message
  final case class WebmList(videos: Seq[String])

  def props(boardApi: BoardApi, store: WebmStore): Props = {
    Props(new WebmStoreDispatcher(boardApi, store))
  }
}

class WebmStoreDispatcher(boardApi: BoardApi, store: WebmStore) extends Actor with ActorLogging {
  private[this] val config = context.system.settings.config.getConfig("webm-tv.sosach")
  private[this] val boards = config.getStringList("boards").toSeq
  private[this] val threadScanner = context.actorOf(WebmThreadScanner.props(boardApi, store), "threadScanner")

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
        case (ThreadId(board, _), files) if boards.contains(board) ⇒
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
