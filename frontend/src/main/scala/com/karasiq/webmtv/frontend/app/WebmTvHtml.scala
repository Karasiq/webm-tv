package com.karasiq.webmtv.frontend.app

import org.scalajs.dom
import rx._

import scala.scalajs.js
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
  import Bootstrap.{button, col, glyphicon, row, toggleButton}

  private def video: Tag = {
    val videoTag = "video".tag
    videoTag("autoplay".attr := "autoplay", "controls".attr := "")
  }

  def videoContainer(videoModifiers: Modifier*): Tag = {
    val video = WebmTvHtml.video(onended := js.ThisFunction.fromFunction1 { (ths: HtmlVideo) ⇒
      if (WebmTvFrontend.loop()) {
        ths.pause()
        ths.currentTime = 0
        ths.play()
      } else {
        WebmTvFrontend.seen.update(WebmTvFrontend.seen() :+ ths.src)
      }
    }, videoModifiers).render.asInstanceOf[HtmlVideo]

    val downloadButton = a(title := "Download video", href := "#", "download".attr := "", target := "_blank")(
      button(
        glyphicon("floppy-disk"), " Download"
      )
    ).render

    Obs(WebmTvFrontend.videoSource, "video-player") {
      WebmTvFrontend.videoSource() match {
        case Some(url) ⇒
          video.src = url
          downloadButton.href = url
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
        button(onclick := { () ⇒ WebmTvFrontend.seen.update(WebmTvFrontend.seen() :+ video.src) })(
          glyphicon("fast-forward"), " Next video"
        ),
        // Reshuffle button
        button(onclick := { () ⇒ WebmTvFrontend.updateVideos() })(
          glyphicon("random"), " Reshuffle"
        ),
        // Loop button
        toggleButton(WebmTvFrontend.loop)(
          glyphicon("repeat"), " Loop"
        ),
        // Download button
        downloadButton
      )),

      // Video player
      row(col(12)(
        video
      ))
    )
  }
}
