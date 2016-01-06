package com.karasiq.webmtv.frontend.utils

import org.scalajs.dom
import rx._

import scala.scalajs.js.ThisFunction
import scalatags.JsDom.all
import scalatags.JsDom.all._

object Bootstrap {
  def button: Tag = {
    val margin = 5.px
    all.button(`type` := "button", `class` := "btn btn-default btn-lg", marginLeft := margin, marginRight := margin)
  }

  def toggleButton(state: Var[Boolean]): Tag = {
    this.button(onclick := ThisFunction.fromFunction1 { (btn: dom.Element) ⇒
      if (btn.classList.contains("active")) {
        btn.classList.remove("active")
        state.update(false)
      } else {
        btn.classList.add("active")
        state.update(true)
      }
    })
  }

  def glyphicon(name: String): Tag = {
    span(`class` := s"glyphicon glyphicon-$name")
  }

  def row: Tag = {
    div(`class` := "row")
  }

  def col(size: Int): Tag = {
    assert(size >= 1 && size <= 12, "Invalid size")
    div(`class` := s"col-md-$size")
  }

  def fullRow(modifiers: Modifier*): Tag = row(col(12)(modifiers))
}
