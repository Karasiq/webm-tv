package com.karasiq.webmtv.app

case class ThreadId(board: String, id: Long)

trait WebmStore extends Iterable[(ThreadId, Seq[String])] {
  def get(id: ThreadId): Option[Seq[String]]
  def update(id: ThreadId, files: Seq[String]): Unit
}
