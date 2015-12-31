package com.karasiq.webmtv.frontend.app

import org.scalajs.jquery.jQuery

import scala.concurrent.Future
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

object WebmTvFrontend extends JSApp {
  def nextVideo(): Future[String] = {
    // TODO: Ajax
    Future.successful("https://2ch.hk/b/src/111694560/14515896032430.webm")
  }

  @JSExport
  override def main(): Unit = {
    jQuery { () ⇒
      val container = jQuery("#main-container")
      container.append {
        WebmTvHtml.videoContainer("https://2ch.hk/b/src/111694560/14515886592770.webm").render
      }
    }
  }
}
