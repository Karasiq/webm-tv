package com.karasiq.webmtv.frontend.utils

import org.scalajs.dom
import upickle.default._

sealed trait AppStorage {
  def load[T: Reader](name: String, default: => T): T

  def save[T: Writer](name: String, value: T): Unit
}

trait AppSessionStorage extends AppStorage {
  override final def load[T: Reader](name: String, default: => T): T =
    Option(dom.window.sessionStorage.getItem(name)).filter(_.nonEmpty)
      .fold(default)(str => read[T](str))

  override final def save[T: Writer](name: String, value: T): Unit =
    dom.window.sessionStorage.setItem(name, write(value))
}

trait AppLocalStorage extends AppStorage {
  override final def load[T: Reader](name: String, default: => T): T =
    Option(dom.window.localStorage.getItem(name)).filter(_.nonEmpty)
      .fold(default)(str => read[T](str))

  override final def save[T: Writer](name: String, value: T): Unit =
    dom.window.localStorage.setItem(name, write(value))
}
