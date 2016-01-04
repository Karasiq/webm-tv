import sbt.Keys._

// Settings
lazy val commonSettings = Seq(
  organization := "com.github.karasiq",
  isSnapshot := false,
  version := "1.0.3",
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
  gulpAssets in Compile := file("frontend") / "webapp",
  gulpCompile in Compile <<= (gulpCompile in Compile).dependsOn(fullOptJS in Compile in frontend)
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
  .enablePlugins(GulpPlugin, JavaAppPackaging)

lazy val frontend = Project("frontend", file("frontend"))
  .settings(commonSettings, frontendSettings)
  .enablePlugins(ScalaJSPlugin)