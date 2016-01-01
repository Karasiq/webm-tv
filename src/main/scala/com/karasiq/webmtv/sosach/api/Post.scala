package com.karasiq.webmtv.sosach.api

import spray.json._

import scala.util.Try

case class Post(comment: String, email: String, files: Seq[PostFile], num: Long, parent: Long, timestamp: Long)

object Post extends DefaultJsonProtocol {
  private object JsLong {
    def unapply(js: JsValue): Option[Long] = js match {
      case JsString(str) ⇒
        Try(str.toLong).toOption

      case JsNumber(num) ⇒
        Try(num.toLongExact).toOption

      case _ ⇒
        None
    }
  }

  implicit val format: JsonFormat[Post] = new JsonFormat[Post] {
    override def write(obj: Post): JsValue = {
      JsObject(
        "comment" → JsString(obj.comment),
        "email" → JsString(obj.email),
        "files" → seqFormat(PostFile.format).write(obj.files),
        "num" → JsString(obj.num.toString),
        "parent" → JsString(obj.parent.toString),
        "timestamp" → JsString(obj.timestamp.toString)
      )
    }

    override def read(json: JsValue): Post = {
      json.asJsObject.getFields("comment", "email", "files", "num", "parent", "timestamp") match {
        case Seq(JsString(comment), JsString(email), files: JsValue, JsLong(num), JsLong(parent), JsLong(timestamp)) ⇒
          Post(comment, email, seqFormat(PostFile.format).read(files), num, parent, timestamp)
      }
    }
  }
}
