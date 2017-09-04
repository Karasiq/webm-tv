package com.karasiq.webmtv.sosach

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, MediaRange, MediaTypes}
import akka.http.scaladsl.model.headers.Accept
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import derive.key
import upickle.Js
import upickle.default._

private object JsonApiObjects {
  case class PostId(value: Long) extends AnyVal
  object PostId {
    implicit def postIdToLong(postId: PostId): Long = postId.value
  }

  case class File(path: String)
  case class Post(@key("num") id: PostId, subject: String, comment: String, files: Seq[File])
  case class Thread(posts: Seq[Post])
  case class Board(@key("Board") name: String, pages: Seq[Int], threads: Seq[Thread])
  case class ThreadWrapped(@key("Board") board: String, @key("threads") thread: Option[Thread])

  object Implicits {
    implicit val postIdReader = Reader[PostId] {
      case Js.Str(str) ⇒
        PostId(str.toLong)

      case Js.Num(num) ⇒
        PostId(num.toLong)
    }
  }
}

class Json2chBoardApi(host: String = "2ch.hk")(implicit as: ActorSystem, am: ActorMaterializer) extends BoardApi {
  import JsonApiObjects.Implicits._

  private val http = Http()

  private def retrieveJson[T: Reader](url: String) = {
    Source
      .fromFuture(http.singleRequest(HttpRequest(uri = url, headers = List(Accept(MediaRange(MediaTypes.`application/json`))))))
      .filter(_.status.isSuccess())
      .flatMapConcat(_.entity.dataBytes)
      .fold(ByteString.empty)(_ ++ _)
      .map(bs ⇒ read[T](bs.utf8String))
      .log("2ch-json-api")
  }

  private def jsonToAppPost(board: String, postObj: JsonApiObjects.Post): Board.Post = {
    Board.Post(postObj.id, postObj.subject, postObj.comment, postObj.files.map(file ⇒ s"https://$host${file.path}"))
  }

  def board(name: String) = {
    val url = s"https://$host/$name/index.json"
    retrieveJson[JsonApiObjects.Board](url)
      .flatMapConcat { page ⇒
        val pages = page.pages.filter(_ > 0).map(page ⇒ s"https://$host/$name/$page.json")
        Source.single(page).concat(
          Source(pages.toVector)
            .flatMapConcat(url ⇒ retrieveJson[JsonApiObjects.Board](url).recoverWithRetries(1, { case _ ⇒ Source.empty }))
        )
      }
      .mapConcat { board ⇒
        val threads = for (threadObj ← board.threads)
          yield Board.Thread(board.name, threadObj.posts.map(jsonToAppPost(board.name, _)))
        threads.toVector
      }
  }

  def thread(board: String, id: Long) = {
    val url = s"https://$host/$board/res/$id.json"
    retrieveJson[JsonApiObjects.ThreadWrapped](url)
      .map(thread ⇒ Board.Thread(thread.board, thread.thread.toSeq.flatMap(_.posts.map(jsonToAppPost(thread.board, _)))))
  }
}
