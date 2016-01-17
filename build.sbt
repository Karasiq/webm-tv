import com.karasiq.scalajsbundler.ScalaJSBundler._
import sbt.Keys._

// Settings
lazy val commonSettings = Seq(
  organization := "com.github.karasiq",
  isSnapshot := false,
  version := "1.0.4",
  scalaVersion := "2.11.7",
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
  resolvers += Resolver.sonatypeRepo("snapshots"),
  libraryDependencies ++= {
    val sprayV = "1.3.3"
    val akkaV = "2.4.0"
    Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaV,
      "io.spray" %% "spray-caching" % sprayV,
      "io.spray" %% "spray-can" % sprayV,
      "io.spray" %% "spray-routing-shapeless2" % sprayV,
      "io.spray" %% "spray-json" % "1.3.2",
      "com.lihaoyi" %% "scalatags" % "0.5.3",
      "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
      "org.scala-lang.modules" %% "scala-async" % "0.9.6-RC2",
      "org.scalatest" %% "scalatest" % "2.2.4" % "test",
      "com.typesafe" % "config" % "1.3.0",
      "org.slf4j" % "slf4j-simple" % "1.7.13",
      "com.github.karasiq" %% "mapdbutils" % "1.1.1",
      "org.mapdb" % "mapdb" % "2.0-beta12"
    )
  },
  mainClass in Compile := Some("com.karasiq.webmtv.app.AppBoot"),
  scalaJsBundlerCompile in Compile <<= (scalaJsBundlerCompile in Compile).dependsOn(fullOptJS in Compile in frontend),
  scalaJsBundlerAssets in Compile += Bundle("index",
    // jQuery
    PageScript(WebAsset("https://code.jquery.com/jquery-1.12.0.js")),

    // Bootstrap
    PageStyle(WebAsset("https://raw.githubusercontent.com/twbs/bootstrap/v3.3.6/dist/css/bootstrap.css")),
    PageScript(WebAsset("https://raw.githubusercontent.com/twbs/bootstrap/v3.3.6/dist/js/bootstrap.js")),
    PageFile("fonts/glyphicons-halflings-regular", WebAsset("https://raw.githubusercontent.com/twbs/bootstrap/v3.3.6/dist/fonts/glyphicons-halflings-regular.eot"), "eot"),
    PageFile("fonts/glyphicons-halflings-regular", WebAsset("https://raw.githubusercontent.com/twbs/bootstrap/v3.3.6/dist/fonts/glyphicons-halflings-regular.svg"), "svg"),
    PageFile("fonts/glyphicons-halflings-regular", WebAsset("https://raw.githubusercontent.com/twbs/bootstrap/v3.3.6/dist/fonts/glyphicons-halflings-regular.ttf"), "ttf"),
    PageFile("fonts/glyphicons-halflings-regular", WebAsset("https://raw.githubusercontent.com/twbs/bootstrap/v3.3.6/dist/fonts/glyphicons-halflings-regular.woff"), "woff"),
    PageFile("fonts/glyphicons-halflings-regular", WebAsset("https://raw.githubusercontent.com/twbs/bootstrap/v3.3.6/dist/fonts/glyphicons-halflings-regular.woff2"), "woff2"),

    // Static
    PageHtml(FileAsset("frontend/webapp/html/index.html")),
    PageStyle(FileAsset("frontend/webapp/css/style.css")),
    PageImage("img/background", FileAsset("frontend/webapp/img/background.jpg")),
    PageImage("favicon", FileAsset("frontend/webapp/img/favicon.ico"), "ico", "image/x-icon"),

    // Scala.js app
    PageScript(FileAsset("frontend/target/scala-2.11/webm-tv-frontend-opt.js")),
    PageScript(FileAsset("frontend/target/scala-2.11/webm-tv-frontend-launcher.js"))
  )
)

lazy val frontendSettings = Seq(
  persistLauncher in Compile := true,
  name := "webm-tv-frontend",
  libraryDependencies ++= Seq(
    "be.doeraene" %%% "scalajs-jquery" % "0.8.1",
    "com.lihaoyi" %%% "scalatags" % "0.5.3",
    "com.lihaoyi" %%% "scalarx" % "0.2.8",
    "com.lihaoyi" %%% "upickle" % "0.3.6"
  )
)

// Projects
lazy val backend = Project("backend", file("."))
  .settings(commonSettings, backendSettings)
  .enablePlugins(ScalaJSBundlerPlugin, JavaAppPackaging)

lazy val frontend = Project("frontend", file("frontend"))
  .settings(commonSettings, frontendSettings)
  .enablePlugins(ScalaJSPlugin)