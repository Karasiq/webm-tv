package com.karasiq.webmtv.sosach.api

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class PostFile(duration: String, height: Int, width: Int, md5: String, name: String, path: String, size: Int, thumbnail: String)

object PostFile extends DefaultJsonProtocol {
  implicit val format: JsonFormat[PostFile] = jsonFormat8(PostFile.apply)
}
