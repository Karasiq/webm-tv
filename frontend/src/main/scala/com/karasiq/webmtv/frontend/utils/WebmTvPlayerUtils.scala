package com.karasiq.webmtv.frontend.utils

import com.karasiq.bootstrap.Bootstrap
import com.karasiq.videojs.Player
import org.scalajs.dom.html.{Element => _, _}

import scala.scalajs.js
import scalatags.JsDom.all._

object WebmTvPlayerUtils {
  implicit class PlayerOps(private val player: Player) extends AnyVal {
    def addButton(title: String, modifiers: Modifier*)(f: Button ⇒ Unit): Unit = {
      player.asInstanceOf[js.Dynamic].controlBar.addChild("button", js.Dynamic.literal(
        el = button(
          `class` := "vjs-control vjs-button",
          aria.live := "polite", `type` := "button",
          modifiers,
          onclick := Bootstrap.jsClick(e ⇒ f(e.asInstanceOf[Button])),
          "touch-action".style := "none",
          span(`class` := "vjs-control-text", title)
        ).render
      ))
    }
  }
}
