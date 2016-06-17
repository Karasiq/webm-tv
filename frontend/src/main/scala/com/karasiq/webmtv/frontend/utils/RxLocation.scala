package com.karasiq.webmtv.frontend.utils

import org.scalajs
import org.scalajs.jquery.jQuery
import rx._

trait RxLocation {
  private val lc: Var[scalajs.dom.Location] = Var(scalajs.dom.window.location)

  def location: Rx[scalajs.dom.Location] = lc

  jQuery(() ⇒ {
    jQuery(scalajs.dom.window).on("hashchange", () ⇒ {
      lc.update(scalajs.dom.window.location)
    })
  })
}

object RxLocation extends RxLocation
