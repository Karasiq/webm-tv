package com.karasiq.webmtv.sosach

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.{Accept, Cookie}
import akka.http.scaladsl.model.{HttpRequest, MediaRange, MediaTypes}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import derive.key
import upickle.Js
import upickle.default._

import scala.language.implicitConversions

private object JsonApiObjects {
  final case class PostId(value: Long) extends AnyVal
  object PostId {
    implicit def postIdToLong(postId: PostId): Long = postId.value

    implicit val postIdReader: Reader[PostId] = Reader[PostId] {
      case Js.Str(str) ⇒
        PostId(str.toLong)

      case Js.Num(num) ⇒
        PostId(num.toLong)
    }

    implicit val postIdWriter: Writer[PostId] = Writer[PostId](postId => Js.Str(postId.value.toString))
  }

  case class File(path: String)
  case class Post(@key("num") id: PostId, subject: String, comment: String, files: Seq[File])
  case class Thread(posts: Seq[Post])
  case class Board(@key("Board") name: String, pages: Seq[Int], threads: Seq[Thread])
  case class ThreadWrapped(@key("Board") board: String, @key("threads") thread: Option[Thread])
}

class Json2chBoardApi(host: String = "2ch.hk", usercodeAuth: String = "")(implicit as: ActorSystem, am: ActorMaterializer) extends BoardApi {
  import JsonApiObjects.PostId._

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
      .named(s"board-$name")
  }

  def thread(board: String, id: Long) = {
    val url = s"https://$host/$board/res/$id.json"
    retrieveJson[JsonApiObjects.ThreadWrapped](url)
      .map(thread ⇒ Board.Thread(thread.board, thread.thread.toSeq.flatMap(_.posts.map(jsonToAppPost(thread.board, _)))))
      .named(s"thread-$board-$id")
  }

  private[this] def retrieveJson[T: Reader](url: String) = {
    Source
      .fromFuture(Http().singleRequest(HttpRequest(uri = url, headers = List(
        Accept(MediaRange(MediaTypes.`application/json`)),
        Cookie("usercode_auth", usercodeAuth)
      ))))
      .filter(_.status.isSuccess())
      .flatMapConcat(_.entity.dataBytes)
      .fold(ByteString.empty)(_ ++ _)
      .filter(_.nonEmpty)
      .map(bs ⇒ read[T](bs.utf8String))
      .log("2ch-json-api")
  }

  private[this] def jsonToAppPost(board: String, postObj: JsonApiObjects.Post): Board.Post = {
    Board.Post(postObj.id, postObj.subject, postObj.comment, postObj.files.map(file ⇒ s"https://$host${file.path}"))
  }
}
