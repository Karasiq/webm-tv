package com.karasiq.webmtv.frontend.app

import org.scalajs.dom
import rx._

import scala.scalajs.js
import scalatags.JsDom.all._

@js.native
trait HtmlVideo extends dom.Element {
  var onended: js.ThisFunction0[HtmlVideo, _] = js.native
  var src: String = js.native

  def load(): Unit = js.native
  def play(): Unit = js.native
  def pause(): Unit = js.native
}

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

  def video: Tag = {
    val videoTag = "video".tag
    videoTag("autoplay".attr := "autoplay", "controls".attr := "")
  }

  def videoContainer(source: Rx[Option[String]], seen: Var[Seq[String]])(videoModifiers: Modifier*): Tag = {
    val video = WebmTvHtml.video(onended := js.ThisFunction.fromFunction1 { (ths: HtmlVideo) ⇒
      seen.update(seen() :+ ths.src)
    }, videoModifiers).render.asInstanceOf[HtmlVideo]

    Obs(source, "video-player") {
      source() match {
        case Some(url) ⇒
          video.src = url
          video.load()
          video.play()

        case None ⇒
          video.pause()
      }
    }

    div(`class` := "jumbotron", textAlign := "center")(
      // Heading
      row(col(12)(
        h1("Webm-TV player")
      )),

      // Next button
      row(col(12)(
        button(`type` := "button", `class` := "btn btn-default btn-lg", onclick := { () ⇒ video.asInstanceOf[js.Dynamic].onended() })(
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
