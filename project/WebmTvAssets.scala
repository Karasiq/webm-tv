import scalatags.Text.all._

object WebmTvAssets {
  private val pageTitle = "title".tag

  def index(): String = {
    "<!DOCTYPE html>" + html(
      head(
        meta(charset := "UTF-8"),
        meta(name := "viewport", content := "width=device-width, initial-scale=1.0"),
        meta(name := "author", content := "Karasiq"),
        meta(name := "description", content := "2ch.hk WebM television"),
        meta(name := "keywords", content := "2ch, webm, /b/"),
        meta(name := "robots", content := "index,nofollow"),
        base(href := "/"),
        pageTitle("WebM-TV"),
        link(rel := "shortcut icon", `type` := "image/x-icon", href := "/favicon.ico"),
        raw("<generated-assets/>")
      ),
      body(
        background := "url(/img/background.jpg)",
        div(id := "main-container", `class` := "container")
      )
    )
  }
}