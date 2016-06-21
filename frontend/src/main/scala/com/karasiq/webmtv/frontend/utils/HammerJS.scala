package com.karasiq.webmtv.frontend.utils

import org.scalajs.dom._
import org.scalajs.dom.html.{Element => _}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSName
import scalatags.JsDom.all._

@js.native
@JSName("Hammer.Recognizer")
private class HammerRecognizer(options: js.Object = ???) extends js.Object {
  def set(options: js.Object): Unit = js.native
}

@js.native
@JSName("Hammer")
private class Hammer(element: Element, options: js.Object = ???) extends js.Object {
  def on(event: String, handler: js.Function): Unit = js.native
  def get(name: String): HammerRecognizer = js.native
}

@js.native
@JSName("Hammer")
object Hammer extends js.Object {
  // Directions
  val DIRECTION_NONE, DIRECTION_LEFT, DIRECTION_RIGHT, DIRECTION_UP, DIRECTION_DOWN, DIRECTION_HORIZONTAL, DIRECTION_VERTICAL, DIRECTION_ALL: Int = js.native
}

case class HammerJS(options: Map[String, js.Object] = Map.empty, events: Map[String, Seq[Element ⇒ Unit]] = Map.empty) extends Modifier {
  def option(name: String, option: (String, js.Any)): HammerJS = {
    val obj = options.getOrElse(name, js.Object())
    obj.asInstanceOf[js.Dynamic].updateDynamic(option._1)(option._2)
    copy(options = options + (name → obj))
  }

  def on(name: String, handler: Element ⇒ Unit): HammerJS = {
    copy(events = events + (name → (events.getOrElse(name, Nil) :+ handler)))
  }

  // Options
  def enable(recognizer: String): HammerJS = option(recognizer, "enable" → true)
  def direction(recognizer: String, direction: Int): HammerJS = option(recognizer, "direction" → direction)

  def applyTo(element: Element) = {
    val hammer = new Hammer(element)
    for ((name, options) ← options)
      hammer.get(name).set(options)
    for ((name, handlers) ← events)
      hammer.on(name, () ⇒ handlers.foreach(_(element)))
  }
}
