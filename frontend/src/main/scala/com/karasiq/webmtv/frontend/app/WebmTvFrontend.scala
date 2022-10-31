package com.karasiq.webmtv.frontend.app

import com.karasiq.webmtv.frontend.utils.AppSessionStorage
import org.scalajs.dom
import scalatags.JsDom.all._

object WebmTvFrontend extends App with AppSessionStorage with WebmTvController with WebmTvHtml {
  dom.window.addEventListener(
    "DOMContentLoaded",
    { _: dom.Event =>
      val container = dom.document.getElementById("main-container")
      container.append {
        videoContainer(id := "webm-tv-video", width := 100.pct, marginTop := 10.px).render
      }
    }
  )
}
