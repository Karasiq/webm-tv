package com.karasiq.webmtv.frontend.utils

import com.karasiq.bootstrap.Bootstrap
import com.karasiq.videojs.Player
import org.scalajs.dom
import org.scalajs.dom._
import org.scalajs.dom.html.{Element => _, _}
import org.scalajs.jquery.{JQueryEventObject, _}

import scala.scalajs.js
import scalatags.JsDom.all._

object WebmTvPlayerUtils {
  private def onTouch(f: dom.Element ⇒ Unit): Modifier = new Modifier {
    def applyTo(el: Element) = {
      val elJquery = jQuery(el)
      elJquery.on("touchstart", (e: JQueryEventObject) ⇒ {
        e.preventDefault()
        elJquery.one("touchend", (e1: JQueryEventObject) ⇒ {
          if (e1.target == e.target) {
            e1.preventDefault()
            f(el)
          }
        })
      })
    }
  }

  implicit class PlayerOps(private val player: Player) extends AnyVal {
    def addButton(title: String, icon: Modifier)(f: Button ⇒ Unit): Unit = {
      player.asInstanceOf[js.Dynamic].controlBar.addChild("button", js.Dynamic.literal(
        el = button(
          `class` := "vjs-control vjs-button",
          aria.live := "polite", `type` := "button",
          icon,
          onclick := Bootstrap.jsClick(e ⇒ f(e.asInstanceOf[Button])),
          onTouch(e ⇒ f(e.asInstanceOf[Button])),
          span(`class` := "vjs-control-text", title)
        ).render
      ))
    }
  }
}
