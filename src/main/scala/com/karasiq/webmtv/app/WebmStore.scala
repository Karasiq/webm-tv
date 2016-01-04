package com.karasiq.webmtv.app

case class ThreadId(board: String, id: Long)

trait WebmStore {
  def get(id: ThreadId): Option[Seq[String]]

  def update(id: ThreadId, files: Seq[String]): Unit

  def iterator: Iterator[(ThreadId, Seq[String])]
}
