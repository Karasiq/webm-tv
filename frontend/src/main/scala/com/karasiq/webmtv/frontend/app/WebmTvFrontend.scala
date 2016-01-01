package com.karasiq.webmtv.frontend.app

import org.scalajs.dom.ext.Ajax
import org.scalajs.jquery.jQuery

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

object WebmTvFrontend extends JSApp {
  private def ajax(url: String): Future[String] = {
    /* val promise = Promise[String]()
    jQuery.ajax(js.Dynamic.literal(
      url = url,
      success = { (data: String, textStatus: String, jqXHR: JQueryXHR) ⇒
        promise.success(data)
      },
      error = { (jqXHR: JQueryXHR, textStatus: String, errorThrow: String) ⇒
        promise.failure(new Exception(errorThrow))
      },
      `type` = "GET"
    ).asInstanceOf[JQueryAjaxSettings])
    promise.future */
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
            WebmTvHtml.videoContainer(url).render
          }
      }
    }
  }
}
