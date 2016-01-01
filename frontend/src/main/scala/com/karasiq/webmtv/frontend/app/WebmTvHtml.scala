package com.karasiq.webmtv.frontend.app

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scalatags.JsDom.all._

private[app] object WebmTvHtml {
  def glyphicon(name: String): Tag = {
    span(`class` := s"glyphicon glyphicon-$name")
  }

  def row: Tag = {
    div(`class` := "row")
  }

  def col(size: Int): Tag = {
    assert(size >= 1 && size <= 12, "Invalid size")
    div(`class` := s"col-md-$size")
  }

  def video(url: String): Tag = {
    val videoTag = "video".tag
    val autoplay = "autoplay".attr
    val controls = "controls".attr

    val nextVideo: js.ThisFunction0[js.Dynamic, Unit] = js.ThisFunction.fromFunction1 { (th: js.Dynamic) ⇒
      WebmTvFrontend.nextVideo().onSuccess {
        case url: String ⇒
          th.src = url
          th.load()
          th.play()
      }
    }

    videoTag(autoplay := "autoplay", controls := "", onended := nextVideo)(
      source(src := url, `type` := "video/webm")
    )
  }

  def videoContainer(url: String)(videoModifiers: Modifier*): Tag = {
    val video = WebmTvHtml.video(url)(videoModifiers).render
    div(`class` := "jumbotron", textAlign := "center")(
      // Heading
      row(col(12)(
        h1("Webm-TV player")
      )),

      // Next button
      row(col(12)(
        button(`type` := "button", `class` := "btn btn-default btn-lg", onclick := { () ⇒ video.asInstanceOf[scalajs.js.Dynamic].onended() })(
          glyphicon("fast-forward"),
          " Next video"
        )
      )),

      // Video player
      row(col(12)(
        video
      ))
    )
  }
}
