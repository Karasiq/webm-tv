package com.karasiq.webmtv.sosach

import akka.NotUsed
import akka.stream.scaladsl.Source

trait BoardApi {
  def board(name: String): Source[Board.Thread, NotUsed]
  def thread(board: String, id: Long): Source[Board.Thread, NotUsed]
}
