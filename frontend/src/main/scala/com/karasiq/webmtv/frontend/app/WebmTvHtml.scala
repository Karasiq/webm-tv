package com.karasiq.webmtv.frontend.app

import com.karasiq.bootstrap.Bootstrap
import com.karasiq.bootstrap.BootstrapImplicits._
import com.karasiq.bootstrap.grid.GridSystem
import com.karasiq.bootstrap.icons.FontAwesome
import com.karasiq.videojs._
import org.scalajs.dom
import org.scalajs.dom.html.Button
import rx._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scalatags.JsDom.all.{button => btn, _}

trait WebmTvHtml { self: WebmTvController ⇒
  def videoContainer(videoModifiers: Modifier*): Tag = {
    val video = VideoJSBuilder()
      .options("bigPlayButton" → false)
      .fluid(true)
      .ready { pl ⇒
        def addButton(icon: Modifier)(f: Button ⇒ Unit): Unit = {
          pl.asInstanceOf[js.Dynamic].controlBar.addChild("button", js.Dynamic.literal(
            el = btn(`class` := "vjs-control vjs-button", aria.live := "polite", `type` := "button")(
              icon,
              onclick := Bootstrap.jsClick(e ⇒ f(e.asInstanceOf[Button]))
            ).render
          ))
        }

        addButton("fast-forward".fontAwesome(FontAwesome.fixedWidth)) { _ ⇒
          seen.update(seen.now :+ pl.src())
        }

        addButton("random".fontAwesome(FontAwesome.fixedWidth)) { btn ⇒
          if (!btn.classList.contains("disabled")) {
            btn.classList.add("disabled")
            updateVideos().onComplete(_ ⇒ btn.classList.remove("disabled"))
          }
        }

        val repeatIcon = Rx[Tag] {
          if (loop())
            "repeat".fontAwesome(FontAwesome.fixedWidth, FontAwesome.spin)
          else
            "repeat".fontAwesome(FontAwesome.fixedWidth)
        }

        addButton(repeatIcon) { _ ⇒
          loop() = !loop.now
        }

        addButton("floppy-o".fontAwesome(FontAwesome.fixedWidth)) { btn ⇒
          val anchor = a(href := videoSource.now.getOrElse("#"), "download".attr := "", target := "_blank", display.none).render
          dom.document.body.appendChild(anchor)
          dom.window.setTimeout(() ⇒ dom.document.body.removeChild(anchor), 700)
          anchor.click()
        }

        pl.on("ended", () ⇒ {
          if (loop.now) {
            pl.pause()
            pl.currentTime(0)
            pl.play()
          } else {
            seen.update(seen.now :+ pl.src())
          }
        })

        pl.on("error", () ⇒ {
          updateVideos()
        })

        videoSource.foreach {
          case Some(url) ⇒
            pl.src(VideoSource("video/webm", url))
            pl.load()
            pl.play()

          case None ⇒
            pl.pause()
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
