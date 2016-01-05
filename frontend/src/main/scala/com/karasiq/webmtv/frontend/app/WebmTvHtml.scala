package com.karasiq.webmtv.frontend.app

import org.scalajs.dom
import rx._

import scala.scalajs.js
import scala.scalajs.js.ThisFunction
import scalatags.JsDom.all._

@js.native
trait HtmlVideo extends dom.Element {
  var onended: js.Function = js.native
  var src: String = js.native
  var currentTime: Int = js.native

  def load(): Unit = js.native
  def play(): Unit = js.native
  def pause(): Unit = js.native
}

private[app] object WebmTvHtml {
  def bootstrapButton: Tag = {
    val margin = 5.px
    button(`type` := "button", `class` := "btn btn-default btn-lg", marginLeft := margin, marginRight := margin)
  }

  def toggleButton(state: Var[Boolean]): Tag = {
    bootstrapButton(onclick := ThisFunction.fromFunction1 { (btn: dom.Element) ⇒
      if (btn.classList.contains("active")) {
        btn.classList.remove("active")
        state.update(false)
      } else {
        btn.classList.add("active")
        state.update(true)
      }
    })
  }

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
    val loop = Var(false)
    val video = WebmTvHtml.video(onended := js.ThisFunction.fromFunction1 { (ths: HtmlVideo) ⇒
      if (loop()) {
        ths.pause()
        ths.currentTime = 0
        ths.play()
      } else {
        seen.update(seen() :+ ths.src)
      }
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
        h1(img(src := "/favicon.ico", maxHeight := 80.px), "Webm-TV player")
      )),

      row(col(12)(
        // Next button
        bootstrapButton(onclick := { () ⇒ seen.update(seen() :+ video.src) })(
          glyphicon("fast-forward"), " Next video"
        ),
        // Loop button
        toggleButton(loop)(
          glyphicon("repeat"), " Loop"
        )
      )),

      // Video player
      row(col(12)(
        video
      ))
    )
  }
}
