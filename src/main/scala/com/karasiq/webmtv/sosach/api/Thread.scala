package com.karasiq.webmtv.sosach.api

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class Thread(posts: Seq[Post]) {
  def opPost: Post = posts.head

  def id: Long = opPost.num
}

object Thread extends DefaultJsonProtocol {
  implicit val format: JsonFormat[Thread] = jsonFormat1(Thread.apply)
}
