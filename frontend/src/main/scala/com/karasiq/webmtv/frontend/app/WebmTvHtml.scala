package com.karasiq.webmtv.frontend.app

import com.karasiq.webmtv.frontend.utils.{Bootstrap, HtmlVideo}
import org.scalajs
import org.scalajs.dom.raw.MouseEvent
import rx._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scalatags.JsDom.all._

trait WebmTvHtml { self: WebmTvController ⇒
  import Bootstrap.{button, fullRow, glyphicon, toggleButton}

  private def video: Tag = {
    val videoTag = "video".tag
    videoTag("autoplay".attr := "autoplay", "controls".attr := "")
  }

  def videoContainer(videoModifiers: Modifier*): Tag = {
    val video = this.video(onended := js.ThisFunction.fromFunction1 { (ths: HtmlVideo) ⇒
      if (loop()) {
        ths.pause()
        ths.currentTime = 0
        ths.play()
      } else {
        seen.update(seen() :+ ths.src)
      }
    }, videoModifiers).render.asInstanceOf[HtmlVideo]

    val downloadButton = a(title := "Download video", href := "#", "download".attr := "", target := "_blank")(
      button(
        glyphicon("floppy-disk"), " Download"
      )
    ).render

    Obs(videoSource, "video-player") {
      videoSource() match {
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
      fullRow(
        h1(img(src := "/favicon.ico", maxHeight := 80.px), "Webm-TV player")
      ),

      fullRow(
        // Next button
        button(onclick := { () ⇒ seen.update(seen() :+ video.src) })(
          glyphicon("fast-forward"), " Next video"
        ),
        // Reshuffle button
        button(onclick := { (e: MouseEvent) ⇒
          val btn = e.target.asInstanceOf[scalajs.dom.Element]
          if (!btn.classList.contains("disabled")) {
            btn.classList.add("disabled")
            updateVideos().onComplete {
              case _ ⇒
                btn.classList.remove("disabled")
            }
          }
        })(
          glyphicon("random"), " Reshuffle"
        ),
        // Loop button
        toggleButton(loop)(
          glyphicon("repeat"), " Loop"
        ),
        // Download button
        downloadButton
      ),

      // Video player
      fullRow(
        video
      )
    )
  }
}
