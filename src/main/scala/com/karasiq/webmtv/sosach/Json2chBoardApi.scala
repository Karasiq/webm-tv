package com.karasiq.webmtv.sosach

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.{`User-Agent`, Accept, Cookie}
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
    implicit def postIdToLong(postId: PostId): Long =
      postId.value

    implicit val postIdReader: Reader[PostId] =
      Reader[PostId] {
        case Js.Str(str) ⇒ PostId(str.toLong)

        case Js.Num(num) ⇒ PostId(num.toLong)
      }

    implicit val postIdWriter: Writer[PostId] = Writer[PostId](postId ⇒ Js.Str(postId.value.toString))
  }

  case class PostFile(path: String)
  case class Post(@key("num") id: PostId, subject: String, comment: String, files: Seq[PostFile])
  case class Thread(posts: Seq[Post])
  case class BoardDescription(id: String, name: String)
  case class Board(@key("board") desc: BoardDescription, pages: Seq[Int], threads: Seq[Thread]) {
    def id: String =
      desc.id

    override def hashCode(): Int =
      id.hashCode
  }
  case class ThreadWrapped(@key("board") board: BoardDescription, @key("threads") thread: Option[Thread])
}

class Json2chBoardApi(
    host: String = "2ch.hk",
    usercodeAuth: String = ""
  )(implicit as: ActorSystem,
    am: ActorMaterializer) extends BoardApi {
  private[this] val log = Logging(as, getClass)

  import JsonApiObjects.PostId._

  def board(name: String) = {
    val url = s"https://$host/$name/index.json"
    retrieveJson[JsonApiObjects.Board](url)
      .flatMapConcat { page ⇒
        val pages = page.pages.filter(_ > 0).map(page ⇒ s"https://$host/$name/$page.json")
        // noinspection ScalaDeprecation
        val restPages =
          Source(pages.toVector)
            .flatMapConcat(url ⇒
              retrieveJson[JsonApiObjects.Board](url).recoverWith { case err ⇒
                log.error(err, "Error reading board page: {}", url)
                Source.empty
              }
            )

        Source.single(page).concat(restPages)
      }
      .mapConcat { board ⇒
        val threads =
          for (threadObj ← board.threads)
            yield Board.Thread(board.id, threadObj.posts.map((postObj: JsonApiObjects.Post) ⇒ jsonToAppPost(postObj)))
        threads.toVector
      }
      .log(s"board-$name")
  }

  def thread(board: String, id: Long) = {
    val url = s"https://$host/$board/res/$id.json"
    retrieveJson[JsonApiObjects.ThreadWrapped](url)
      .map(thread ⇒
        Board.Thread(
          thread.board.id,
          thread.thread.toSeq.flatMap(_.posts.map((postObj: JsonApiObjects.Post) ⇒ jsonToAppPost(postObj)))
        )
      )
      .named(s"thread-$board-$id")
  }

  private[this] def retrieveJson[T](url: String)(implicit r: Reader[T], m: Manifest[T]): Source[T, NotUsed] =
    Source
      .fromFuture(Http().singleRequest(HttpRequest(
        uri = url,
        headers =
          List(
            `User-Agent`("curl/7.54"),
            Accept(MediaRange(MediaTypes.`application/json`)),
            Cookie("usercode_auth", usercodeAuth)
          )
      )))
      .flatMapConcat {
        case successResponse if successResponse.status.isSuccess() ⇒
          successResponse.entity.dataBytes
            .fold(ByteString.empty)(_ ++ _)
            // .map { bs ⇒ log.info("\n_________________________\n{}\n_________________________\n", bs.utf8String); bs }
            .map(bs ⇒ read[T](bs.utf8String))

        case errorResponse ⇒
          errorResponse.discardEntityBytes()
          Source.failed(new IllegalArgumentException(s"Request failed: $url -> ${errorResponse.status}"))
      }
      .log(s"2ch-json-api-${m.runtimeClass.getName}")

  private[this] def jsonToAppPost(postObj: JsonApiObjects.Post) =
    try Board.Post(
        postObj.id,
        postObj.subject,
        postObj.comment,
        Option(postObj.files).getOrElse(Nil).map(file ⇒ s"https://$host${file.path}")
      )
    catch {
      case err: Exception ⇒ throw new IllegalArgumentException(s"Error parsing post: $postObj", err)
    }
}
