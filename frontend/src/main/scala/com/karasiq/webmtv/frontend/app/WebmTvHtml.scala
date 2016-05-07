package com.karasiq.webmtv.frontend.app

import com.karasiq.videojs._
import com.karasiq.webmtv.frontend.utils.Bootstrap
import org.scalajs
import org.scalajs.dom.raw.MouseEvent
import rx._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scalatags.JsDom.all._

trait WebmTvHtml { self: WebmTvController ⇒
  import Bootstrap.{button, fullRow, glyphicon, toggleButton}

  def videoContainer(videoModifiers: Modifier*): Tag = {
    val downloadButton = a(title := "Download video", href := "#", "download".attr := "", target := "_blank")(
      button(
        glyphicon("floppy-disk"), " Download"
      )
    ).render

    var player: js.UndefOr[Player] = js.undefined
    val video = VideoJSBuilder()
      .options("bigPlayButton" → false)
      .fluid(true)
      .ready { pl ⇒
        player = pl
        pl.on("ended", () ⇒ {
          if (loop.now) {
            pl.pause()
            pl.currentTime(0)
            pl.play()
          } else {
            seen.update(seen.now :+ pl.src())
          }
        })

        Obs(videoSource, "video-player") {
          videoSource.now match {
            case Some(url) ⇒
              pl.src(VideoSource("video/webm", url))
              downloadButton.href = url
              pl.load()
              pl.play()

            case None ⇒
              pl.pause()
          }
        }
      }
      .build()

    div(`class` := "jumbotron", textAlign := "center")(
      // Heading
      fullRow(
        h1(img(src := "/favicon.ico", maxHeight := 80.px), "Webm-TV player")
      ),

      fullRow(
        // Next button
        button(onclick := { () ⇒
          player.foreach { video ⇒
            seen.update(seen.now :+ video.src())
          }
        })(
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
        div(video, videoModifiers)
      )
    )
  }
}
