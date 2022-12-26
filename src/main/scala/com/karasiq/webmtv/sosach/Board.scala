package com.karasiq.webmtv.sosach

import scala.language.implicitConversions

object Board {
  case class Post(id: Long, title: String, content: String, files: Seq[String])
  case class Thread(board: String, posts: Seq[Post]) {
    assert(posts.nonEmpty, "Thread couldn't be empty")

    def opPost: Post =
      posts.head

    def answers: Seq[Post] =
      posts.drop(1)
  }

  implicit def threadAsPost(th: Thread): Post =
    th.opPost
}
