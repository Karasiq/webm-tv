package com.karasiq.webmtv.frontend.utils

import org.scalajs.dom.window
import rx._

import scala.scalajs.js

sealed trait RxLocation {
  private val _hash: Var[String] = Var(window.location.hash)

  def hash(implicit ctx: Ctx.Owner): Rx[Option[String]] =
    _hash.map { value =>
      if (js.isUndefined(value) || value.eq(null) || value.isEmpty) {
        None
      } else {
        Some(value.tail)
      }
    }

  window.addEventListener("hashchange", (_: Any) => _hash.update(window.location.hash))
}

object RxLocation extends RxLocation
