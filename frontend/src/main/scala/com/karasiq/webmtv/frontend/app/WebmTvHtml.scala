package com.karasiq.webmtv.frontend.app

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scalatags.JsDom.all._
import org.scalajs.dom.{Element, KeyboardEvent, document, window}
import rx._
import com.karasiq.bootstrap.BootstrapImplicits._
import com.karasiq.bootstrap.grid.GridSystem
import com.karasiq.bootstrap.icons.FontAwesome
import com.karasiq.bootstrap.popover.Popover
import com.karasiq.videojs._
import com.karasiq.webmtv.frontend.utils.HammerJS
import com.karasiq.webmtv.frontend.utils.WebmTvPlayerUtils._
import org.scalajs.dom.raw.{HTMLElement, MouseEvent}

trait WebmTvHtml {
  self: WebmTvController ⇒
  def videoContainer(videoModifiers: Modifier*): Tag = {
    val sfwMode = Var(false)

    val sfwModifier = new Modifier {
      override def applyTo(t: Element): Unit = {
        t.addEventListener("mousemove", (e: MouseEvent) => {
          if (sfwMode.now) {
            val maxDist = math.sqrt(math.pow(t.clientWidth, 2) + math.pow(t.clientHeight, 2))/2

            val rect = t.getBoundingClientRect()
            val (centerX, centerY) = (
              rect.left + rect.width / 2,
              rect.top + rect.height / 2
            )

            val (mouseX, mouseY) = (e.pageX, e.pageY)
            val dist = math.hypot(centerX - mouseX, centerY - mouseY)

            org.scalajs.dom.console.log(dist + " of " + maxDist)
            val opacityV = ((maxDist - dist) / maxDist) min 1 max 0.01
            t.asInstanceOf[HTMLElement].style.opacity = opacityV.toString
          } else {
            t.asInstanceOf[HTMLElement].style.opacity = "1"
          }
        })
      }
    }

    val video = VideoJSBuilder()
      .options("bigPlayButton" → false, "loadingSpinner" → false)
      .fluid(true)
      .ready { player ⇒
        def nextVideo(): Unit = {
          history() = history.now ++ videoSource.now
          seen() = seen.now ++ videoSource.now
        }

        def previousVideo(): Unit = {
          val video = history.now.lastOption
          history() = history.now.dropRight(1)
          seen() = seen.now -- video
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
          val seenVideos = history()
          val currentVideo = videoSource()
          seenVideos.nonEmpty && currentVideo.isDefined && {
            val allVideos = videos()
            val prevIndex = allVideos.indexOf(seenVideos.last)
            val currentIndex = allVideos.indexOf(currentVideo.get)
            prevIndex != -1 && prevIndex == (currentIndex - 1)
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
          val pf: PartialFunction[Int, Unit] = {
            case 37 if showPrevious.now ⇒ // Left arrow
              previousVideo()

            case 39 ⇒ // Right arrow
              nextVideo()

            case 68 if e.shiftKey ⇒ // Shift+D
              downloadVideo()

            case 76 if e.shiftKey ⇒ // Shift+L
              toggleLoop()

            case 82 if e.shiftKey ⇒ // Shift+R
              e.preventDefault()
              reshuffle()

            case 83 if e.shiftKey => // Shift+S
              sfwMode() = !sfwMode.now
          }

          if (pf.isDefinedAt(e.keyCode)) {
            e.preventDefault()
            pf(e.keyCode)
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

        var errors = 0
        player.on("error", () => {
          if (errors < 10) {
            errors += 1
            nextVideo()
          } else {
            org.scalajs.dom.console.error("Too many errors")
          }
        })

        player.on("canplay", () => {
          errors = 0
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

    val boards = Seq("b", "v", "po", "tv", "e", "a")
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
                li("Shift+D - download"),
                li("Shift+S - ", a(href := "#", "SFW mode", "text-success".classIf(sfwMode), onclick := { e: MouseEvent =>
                  e.preventDefault()
                  sfwMode() = !sfwMode.now
                }))
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
        div(video, sfwModifier, videoModifiers)
      )
    )
  }
}
