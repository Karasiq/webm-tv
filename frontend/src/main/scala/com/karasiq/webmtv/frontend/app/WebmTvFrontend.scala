package com.karasiq.webmtv.frontend.app

import org.scalajs.dom.ext.Ajax
import org.scalajs.jquery.jQuery

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import scalatags.JsDom.all._

object WebmTvFrontend extends JSApp {
  private def ajax(url: String): Future[String] = {
    Ajax.get(url).map(_.responseText)
  }

  def nextVideo(): Future[String] = ajax("/video")

  @JSExport
  override def main(): Unit = {
    jQuery { () ⇒
      val container = jQuery("#main-container")
      nextVideo().onSuccess {
        case url ⇒
          container.append {
            WebmTvHtml.videoContainer(url)(marginTop := 10.px, width := container.width() - 120).render
          }
      }
    }
  }
}
