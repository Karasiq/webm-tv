package com.karasiq.webmtv.frontend.app

import com.karasiq.bootstrap.BootstrapImplicits._
import com.karasiq.bootstrap.grid.GridSystem
import com.karasiq.bootstrap.icons.FontAwesome
import com.karasiq.videojs._
import com.karasiq.webmtv.frontend.utils.WebmTvPlayerUtils._
import org.scalajs.dom.{document, window}
import rx._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scalatags.JsDom.all._

trait WebmTvHtml { self: WebmTvController ⇒
  def videoContainer(videoModifiers: Modifier*): Tag = {
    val video = VideoJSBuilder()
      .options("bigPlayButton" → false, "loadingSpinner" → false)
      .fluid(true)
      .ready { player ⇒
        player.addButton("Next video", "fast-forward".fontAwesome()) { _ ⇒
          seen.update(seen.now :+ player.src())
        }

        player.addButton("Reshuffle", "random".fontAwesome()) { btn ⇒
          if (!btn.classList.contains("disabled")) {
            btn.classList.add("disabled")
            updateVideos().onComplete(_ ⇒ btn.classList.remove("disabled"))
          }
        }

        val repeatIcon = Rx[Tag] {
          if (loop())
            "repeat".fontAwesome(FontAwesome.spin)
          else
            "repeat".fontAwesome()
        }

        player.addButton("Loop", repeatIcon) { _ ⇒
          loop() = !loop.now
        }

        player.addButton("Download", "floppy-o".fontAwesome()) { btn ⇒
          val anchor = a(href := videoSource.now.getOrElse("#"), "download".attr := "", target := "_blank", display.none).render
          document.body.appendChild(anchor)
          window.setTimeout(() ⇒ document.body.removeChild(anchor), 700)
          anchor.click()
        }

        player.on("ended", () ⇒ {
          if (loop.now) {
            player.pause()
            player.currentTime(0)
            player.play()
          } else {
            seen.update(seen.now :+ player.src())
          }
        })

        player.on("error", () ⇒ {
          window.setTimeout(() ⇒ updateVideos(), 100)
        })

        videoSource.foreach {
          case Some(url) ⇒
            player.src(VideoSource("video/webm", url))
            player.load()
            player.play()

          case None ⇒
            player.pause()
        }
      }
      .build()

    div(`class` := "jumbotron", textAlign := "center")(
      // Heading
      GridSystem.mkRow(
        h1(img(src := "/favicon.ico", maxHeight := 80.px), "Webm-TV player")
      ),

      // Video player
      GridSystem.mkRow(
        div(video, videoModifiers)
      )
    )
  }
}
