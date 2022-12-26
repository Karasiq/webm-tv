package com.karasiq.webmtv.frontend.utils

import org.scalajs.dom.ext.{LocalStorage, SessionStorage}
import upickle.default._

sealed trait AppStorage {
  def load[T: Reader](name: String, default: ⇒ T): T

  def save[T: Writer](name: String, value: T): Unit
}

trait AppSessionStorage extends AppStorage {
  override final def load[T: Reader](name: String, default: ⇒ T): T =
    SessionStorage(name).fold(default)(str ⇒ read[T](str))

  override final def save[T: Writer](name: String, value: T): Unit =
    SessionStorage.update(name, write(value))
}

trait AppLocalStorage extends AppStorage {
  override final def load[T: Reader](name: String, default: ⇒ T): T =
    LocalStorage(name).fold(default)(str ⇒ read[T](str))

  override final def save[T: Writer](name: String, value: T): Unit =
    LocalStorage.update(name, write(value))
}
