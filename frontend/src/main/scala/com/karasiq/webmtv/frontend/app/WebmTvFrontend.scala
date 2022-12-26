package com.karasiq.webmtv.frontend.app

import com.karasiq.webmtv.frontend.utils.AppSessionStorage
import org.scalajs.jquery.jQuery

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import scalatags.JsDom.all._

object WebmTvFrontend extends JSApp with AppSessionStorage with WebmTvController with WebmTvHtml {
  @JSExport
  override def main(): Unit =
    jQuery { () ⇒
      val container = jQuery("#main-container")
      container.append {
        videoContainer(id := "webm-tv-video", width := 100.pct, marginTop := 10.px).render
      }
    }
}
