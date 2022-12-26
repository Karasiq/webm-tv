package com.karasiq.webmtv.frontend.utils

import com.karasiq.videojs.Player

import scala.scalajs.js
import scalatags.JsDom.all._

object WebmTvPlayerUtils {
  implicit class PlayerOps(private val player: Player) extends AnyVal {
    def addButton(title: String, modifiers: Modifier*)(f: ⇒ Unit): Unit =
      player.controlBar.addChild(
        "button",
        js.Dynamic.literal(
          el =
            button(
              `type`    := "button",
              `class`   := "vjs-control vjs-button",
              aria.live := "polite",
              modifiers,
              HammerJS().on("tap", _ ⇒ f),
              span(`class` := "vjs-control-text", title)
            ).render
        )
      )
  }
}
