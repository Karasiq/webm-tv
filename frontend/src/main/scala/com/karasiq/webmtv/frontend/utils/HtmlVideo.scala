package com.karasiq.webmtv.frontend.utils

import org.scalajs.dom

import scala.scalajs.js

@js.native
trait HtmlVideo extends dom.Element {
  var onended: js.Function = js.native
  var src: String = js.native
  var currentTime: Int = js.native

  def load(): Unit = js.native
  def play(): Unit = js.native
  def pause(): Unit = js.native
}
