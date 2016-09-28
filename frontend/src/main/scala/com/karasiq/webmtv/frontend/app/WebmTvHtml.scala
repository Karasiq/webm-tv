package com.karasiq.webmtv.frontend.app

import com.karasiq.bootstrap.BootstrapImplicits._
import com.karasiq.bootstrap.grid.GridSystem
import com.karasiq.bootstrap.icons.FontAwesome
import com.karasiq.bootstrap.popover.Popover
import com.karasiq.videojs._
import com.karasiq.webmtv.frontend.utils.HammerJS
import com.karasiq.webmtv.frontend.utils.WebmTvPlayerUtils._
import org.scalajs.dom.{KeyboardEvent, document, window}
import rx._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scalatags.JsDom.all._

trait WebmTvHtml { self: WebmTvController ⇒
  def videoContainer(videoModifiers: Modifier*): Tag = {
    val video = VideoJSBuilder()
      .options("bigPlayButton" → false, "loadingSpinner" → false)
      .fluid(true)
      .ready { player ⇒
        def nextVideo(): Unit = {
          seen() = seen.now ++ videoSource.now
        }

        def previousVideo(): Unit = {
          seen() = seen.now.dropRight(1)
        }

        var reshuffling = false
        def reshuffle(): Unit = {
          if (!reshuffling) {
            reshuffling = true
            updateVideos().onComplete(_ ⇒ reshuffling = false)
          }
        }

        def toggleLoop(): Unit = {
          loop() = !loop.now
        }

        def downloadVideo(): Unit = {
          val anchor = a(href := videoSource.now.getOrElse("#"), "download".attr := "", target := "_blank", display.none).render
          document.body.appendChild(anchor)
          window.setTimeout(() ⇒ document.body.removeChild(anchor), 700)
          anchor.click()
        }

        val showPrevious = Rx {
          val seenVideos = seen()
          val currentVideo = videoSource()
          seenVideos.nonEmpty && currentVideo.isDefined && {
            val allVideos = videos()
            val index = allVideos.indexOf(seenVideos.last)
            index != -1 && index == (allVideos.indexOf(currentVideo.get) - 1)
          }
        }
        val loopIcon = Rx[Tag](if (loop()) "repeat".fontAwesome(FontAwesome.spin) else "repeat".fontAwesome())
        val vjsXsHide = "vjs-xs-hide".addClass

        // Player buttons
        player.addButton("Previous video", "fast-backward".fontAwesome(), showPrevious.reactiveShow, vjsXsHide)(previousVideo())
        player.addButton("Next video", "fast-forward".fontAwesome())(nextVideo())
        player.addButton("Reshuffle", "random".fontAwesome(), vjsXsHide)(reshuffle())
        player.addButton("Loop", loopIcon)(toggleLoop())
        player.addButton("Download", "floppy-o".fontAwesome(), vjsXsHide)(downloadVideo())

        // Hotkeys
        document.addEventListener("keydown", (e: KeyboardEvent) ⇒ {
          e.keyCode match {
            case 37 if showPrevious.now ⇒ // Left arrow
              e.preventDefault()
              previousVideo()

            case 39 ⇒ // Right arrow
              e.preventDefault()
              nextVideo()

            case 68 if e.shiftKey ⇒ // Shift+D
              e.preventDefault()
              downloadVideo()

            case 76 if e.shiftKey ⇒ // Shift+L
              e.preventDefault()
              toggleLoop()

            case 82 if e.shiftKey ⇒ // Shift+R
              e.preventDefault()
              reshuffle()

            case _ ⇒
            // Skip
          }
        })

        // Touch gestures
        HammerJS()
          .enable("rotate")
          .on("swiperight", _ ⇒ if (showPrevious.now) previousVideo())
          .on("swipeleft", _ ⇒ nextVideo())
          .on("rotate", _ ⇒ reshuffle())
          .applyTo(player.el())

        player.on("ended", () ⇒ {
          if (loop.now) {
            player.pause()
            player.currentTime(0)
            player.play()
          } else {
            nextVideo()
          }
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

    val boards = Seq("b", "po", "tv", "e", "a")
    div(`class` := "jumbotron", textAlign := "center", backgroundColor := "rgba(238, 238, 238, 0.2)")(
      // Heading
      GridSystem.mkRow(
        h1(
          img(src := "/favicon.ico", maxHeight := 80.px),
          span(
            "WebM-TV",
            cursor.help,
            borderBottom := "1px dotted #777",
            Popover("2ch.hk WebM TV", div(
              b("Controls"),
              ul(
                li("⇐ - previous video"),
                li("⇒ - next video"),
                li("Shift+R - reshuffle"),
                li("Shift+L - toggle loop"),
                li("Shift+D - download")
              ),
              b("Gestures"),
              ul(
                li("Left swipe - next video"),
                li("Right swipe - previous video"),
                li("Rotate - reshuffle")
              ),
              b("Select board with URL hash"),
              ul(
                for (boardUrl ← boards.map("#" + _)) yield
                  li(a(href := boardUrl, boardUrl))
              ),
              a(href := "https://github.com/Karasiq/webm-tv", target := "_blank", i("Copyright 2016, Karasiq"))
            ))
          ),
          fontFamily := "Impact,Haettenschweiler,Franklin Gothic Bold,Charcoal,Helvetica Inserat,Bitstream Vera Sans Bold,Arial Black,sans serif"
        )
      ),

      // Video player
      GridSystem.mkRow(
        div(video, videoModifiers)
      )
    )
  }
}
