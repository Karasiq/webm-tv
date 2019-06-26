import scalatags.Text.all._

object WebmTvAssets {
  private val pageTitle = "title".tag

  def index: String = {
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
        link(rel := "shortcut icon", `type` := "image/x-icon", href := "/favicon.ico")
      ),
      body(
        div(id := "main-container", `class` := "container", marginTop := 40.px, marginBottom := 40.px)
      )
    )
  }

  def style: String = {
    """
      |body {
      |  background: url(/img/background.jpg) no-repeat center center fixed;
      |  -webkit-background-size: 100%;
      |  -moz-background-size: 100%;
      |  -o-background-size: 100%;
      |  background-size: 100%;
      |  -webkit-background-size: cover;
      |  -moz-background-size: cover;
      |  -o-background-size: cover;
      |  background-size: cover;
      |}
      |@media (max-width: 424px) {
      |  .vjs-xs-hide {
      |    display: none !important;
      |  }
      |}
      |@media (min-width: 768px) {
      |  #main-container {
      |    max-width: 80%;
      |  }
      |}
      |.glyphicon {
      |  margin-left: 2px;
      |  margin-right: 2px;
      |}
    """.stripMargin
  }
}
