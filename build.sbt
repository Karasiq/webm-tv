import com.karasiq.scalajsbundler.dsl.{Script, _}

// Settings
lazy val commonSettings = Seq(
  organization := "com.github.karasiq",
  isSnapshot := false,
  version := "1.0.8",
  scalaVersion := "2.11.8",
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  publishArtifact in Test := false,
  pomIncludeRepository := { _ ⇒ false },
  licenses := Seq("Apache License, Version 2.0" → url("http://opensource.org/licenses/Apache-2.0")),
  homepage := Some(url("https://github.com/Karasiq/webm-tv")),
  pomExtra :=
    <scm>
      <url>git@github.com:Karasiq/webm-tv.git</url>
      <connection>scm:git:git@github.com:Karasiq/webm-tv.git</connection>
    </scm>
    <developers>
      <developer>
        <id>karasiq</id>
        <name>Piston Karasiq</name>
        <url>https://github.com/Karasiq</url>
      </developer>
    </developers>
)

lazy val backendSettings = Seq(
  name := "webm-tv",
  libraryDependencies ++= {
    val akkaV = "2.4.6"
    Seq(
      "org.jsoup" % "jsoup" % "1.9.2",
      "com.typesafe.akka" %% "akka-actor" % akkaV,
      "com.typesafe.akka" %% "akka-http-experimental" % akkaV,
      "com.lihaoyi" %% "scalatags" % "0.5.4",
      "com.lihaoyi" %% "upickle" % "0.3.8",
      "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
      "org.scala-lang.modules" %% "scala-async" % "0.9.6-RC2",
      "org.scalatest" %% "scalatest" % "2.2.4" % "test",
      "com.typesafe" % "config" % "1.3.0",
      "org.slf4j" % "slf4j-simple" % "1.7.13",
      "com.github.karasiq" %% "mapdbutils" % "1.1.1",
      "org.mapdb" % "mapdb" % "2.0-beta12"
    )
  },
  mainClass in Compile := Some("com.karasiq.webmtv.app.Main"),
  scalaJsBundlerInline in Compile := true,
  scalaJsBundlerCompile in Compile <<= (scalaJsBundlerCompile in Compile).dependsOn(fullOptJS in Compile in frontend),
  scalaJsBundlerAssets in Compile += {
    val bootstrap = github("twbs", "bootstrap", "3.3.6") / "dist"
    val videoJs = github("videojs", "video.js", "5.8.0") / "dist"
    val jsDeps = Seq(
      // jQuery
      Script from url("https://code.jquery.com/jquery-1.12.0.js"),
      // Boostrap
      Style from url(bootstrap % "css/bootstrap.css"),
      Script from url(bootstrap % "js/bootstrap.js"),
      Style from url("https://raw.githubusercontent.com/FortAwesome/Font-Awesome/v4.5.0/css/font-awesome.css"),
      // Video.js
      Script from url(videoJs % "video.min.js"),
      Style from url(videoJs % "video-js.min.css"),
      Static("video-js.swf") from url(videoJs % "video-js.swf")
    )
    val fonts = fontPackage("fontawesome-webfont", "https://raw.githubusercontent.com/FortAwesome/Font-Awesome/v4.5.0/fonts/fontawesome-webfont")
    val appFiles = Seq(
      Html from WebmTvAssets.index,
      Style from WebmTvAssets.style,
      Image("img/background.jpg") from file("frontend/webapp/img/background.jpg"),
      Image("favicon.ico").withMime("image/x-icon") from file("frontend/webapp/img/favicon.ico"),

      // Scala.js app
      Script from file("frontend/target/scala-2.11/webm-tv-frontend-opt.js"),
      Script from file("frontend/target/scala-2.11/webm-tv-frontend-launcher.js")
    )
    Bundle("index", jsDeps ++ appFiles ++ fonts:_*)
  }
)

lazy val frontendSettings = Seq(
  persistLauncher in Compile := true,
  name := "webm-tv-frontend",
  resolvers += Resolver.sonatypeRepo("snapshots"),
  libraryDependencies ++= Seq(
    "be.doeraene" %%% "scalajs-jquery" % "0.8.1",
    "com.lihaoyi" %%% "scalatags" % "0.5.4",
    "com.lihaoyi" %%% "scalarx" % "0.3.1",
    "com.lihaoyi" %%% "upickle" % "0.3.6",
    "com.github.karasiq" %%% "scalajs-videojs" % "1.0.3",
    "com.github.karasiq" %%% "scalajs-bootstrap" % "1.0.7"
  )
)

// Projects
lazy val backend = project.in(file("."))
  .settings(commonSettings, backendSettings)
  .enablePlugins(ScalaJSBundlerPlugin, JavaAppPackaging)

lazy val frontend = project.in(file("frontend"))
  .settings(commonSettings, frontendSettings)
  .enablePlugins(ScalaJSPlugin)