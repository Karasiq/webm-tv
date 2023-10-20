import com.karasiq.scalajsbundler.dsl.{Script, _}

// Settings
lazy val commonSettings =
  Seq(
    organization      := "com.github.karasiq",
    version           := "1.2.2",
    isSnapshot        := version.value.endsWith("SNAPSHOT"),
    scalaVersion      := "2.11.11",
    publishMavenStyle := true,
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    publishArtifact in Test := false,
    pomIncludeRepository    := { _ ⇒ false },
    licenses                := Seq("Apache License, Version 2.0" → url("http://opensource.org/licenses/Apache-2.0")),
    homepage                := Some(url("https://github.com/Karasiq/webm-tv")),
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

lazy val backendSettings =
  Seq(
    name := "webm-tv",
    libraryDependencies ++= {
      val akkaV = "2.4.10"
      Seq(
        "org.jsoup"                % "jsoup"                  % "1.16.2",
        "com.typesafe.akka"       %% "akka-actor"             % akkaV,
        "com.typesafe.akka"       %% "akka-http-experimental" % akkaV,
        "com.lihaoyi"             %% "scalatags"              % "0.5.4",
        "com.lihaoyi"             %% "upickle"                % "0.4.1",
        "net.databinder.dispatch" %% "dispatch-core"          % "0.11.2",
        "org.scala-lang.modules"  %% "scala-async"            % "0.9.6-RC2",
        "org.scalatest"           %% "scalatest"              % "2.2.4" % "test",
        "com.typesafe"             % "config"                 % "1.3.0",
        "org.slf4j"                % "slf4j-simple"           % "1.7.13",
        "com.google.guava"         % "guava"                  % "28.0-jre",
        "com.github.karasiq"      %% "commons-configs"        % "1.0.11"
      )
    },
    mainClass in Compile            := Some("com.karasiq.webmtv.app.WebmTvMain"),
    scalaJsBundlerInline in Compile := false,
    scalaJsBundlerCompile in Compile <<= (scalaJsBundlerCompile in Compile).dependsOn(fullOptJS in Compile in frontend),
    scalaJsBundlerAssets in Compile += {
      val bootstrap = github("twbs", "bootstrap", "v3.3.6") / "dist"
      val videoJs   = github("videojs", "video.js", "v5.8.8") / "dist"
      val jsDeps =
        Seq(
          // jQuery
          Script from url("https://code.jquery.com/jquery-2.2.4.min.js"),
          // Boostrap
          Style from url(bootstrap % "css/bootstrap.css"),
          Script from url(bootstrap % "js/bootstrap.js"),
          Style from url("https://raw.githubusercontent.com/FortAwesome/Font-Awesome/v4.5.0/css/font-awesome.css"),
          // Video.js
          Script from url(videoJs % "video.min.js"),
          Style from url(videoJs % "video-js.min.css"),
          Static("video-js.swf") from url(videoJs % "video-js.swf"),
          // Hammer.js
          Script from url("https://hammerjs.github.io/dist/hammer.min.js")
        )
      val fonts =
        fontPackage(
          "fontawesome-webfont",
          "https://raw.githubusercontent.com/FortAwesome/Font-Awesome/v4.5.0/fonts/fontawesome-webfont"
        )
      val appFiles =
        Seq(
          Html from WebmTvAssets.index,
          Style from WebmTvAssets.style,
          Image("img/background.jpg") from file("frontend/webapp/img/background.jpg"),
          Image("favicon.ico").withMime("image/x-icon") from file("frontend/webapp/img/favicon.ico"),

          // Scala.js app
          Script from file("frontend/target/scala-2.11/webm-tv-frontend-opt.js"),
          Script from file("frontend/target/scala-2.11/webm-tv-frontend-launcher.js")
        )
      Bundle("index", jsDeps, appFiles, fonts)
    }
  )

lazy val frontendSettings =
  Seq(
    persistLauncher in Compile := true,
    name                       := "webm-tv-frontend",
    resolvers                  += Resolver.sonatypeRepo("snapshots"),
    libraryDependencies ++= Seq(
      "be.doeraene"        %%% "scalajs-jquery"    % "0.9.0",
      "com.lihaoyi"        %%% "scalatags"         % "0.5.4",
      "com.lihaoyi"        %%% "scalarx"           % "0.3.1",
      "com.lihaoyi"        %%% "upickle"           % "0.3.6",
      "com.github.karasiq" %%% "scalajs-videojs"   % "1.0.4",
      "com.github.karasiq" %%% "scalajs-bootstrap" % "1.0.9"
    )
  )

lazy val dockerSettings =
  Seq(
    dockerBaseImage    := "openjdk:8-jre-stretch",
    dockerExposedPorts := Seq(8900),
    dockerUsername     := Some("karasiq"),
    dockerUpdateLatest := true
  )

// Projects
lazy val backend =
  project.in(file("."))
    .settings(commonSettings, backendSettings, dockerSettings)
    .enablePlugins(ScalaJSBundlerPlugin, JavaAppPackaging, DockerPlugin)

lazy val frontend =
  project.in(file("frontend"))
    .settings(commonSettings, frontendSettings)
    .enablePlugins(ScalaJSPlugin)
