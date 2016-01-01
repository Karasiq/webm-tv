package com.karasiq.webmtv.sosach.api

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class Board(Board: String, BoardName: String, threads: Seq[Thread])

object Board extends DefaultJsonProtocol {
  implicit val format: JsonFormat[Board] = jsonFormat3(Board.apply)
}
