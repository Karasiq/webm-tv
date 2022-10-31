import com.karasiq.scalajsbundler.dsl.{Script, _}

// Settings
lazy val commonSettings =
  Seq(
    organization       := "com.github.karasiq",
    version            := "1.2.2",
    isSnapshot         := version.value.endsWith("SNAPSHOT"),
    scalaVersion       := "2.13.9",
    //evictionErrorLevel := Level.Debug
  )

lazy val backendSettings =
  Seq(
    name := "webm-tv",
    libraryDependencies ++= {
      val akkaV = "2.7.0"
      Seq(
        "org.jsoup"               % "jsoup"        % "1.15.3",
        "com.typesafe.akka"      %% "akka-actor"   % akkaV,
        "com.typesafe.akka"      %% "akka-stream"  % akkaV,
        "com.typesafe.akka"      %% "akka-http"    % "10.4.0",
        "com.lihaoyi"            %% "scalatags"    % "0.12.0",
        "com.lihaoyi"            %% "upickle"      % "2.0.0",
        "org.scala-lang.modules" %% "scala-async"  % "1.0.1",
        "org.scalatest"          %% "scalatest"    % "3.2.14" % Test,
        "com.typesafe"            % "config"       % "1.4.2",
        "org.slf4j"               % "slf4j-simple" % "2.0.3",
        "com.google.guava"        % "guava"        % "31.1-jre"
      )
    }
  ) ++ inConfig(Compile)(Seq(
    mainClass            := Some("com.karasiq.webmtv.app.WebmTvMain"),
    scalaJsBundlerInline := false,
    scalaJsBundlerAssets += {
      val bootstrap   = github("twbs", "bootstrap", "v3.3.6") / "dist"
      val VideoJSDist = "https://cdnjs.cloudflare.com/ajax/libs/video.js/7.20.3/"
      val jsDeps =
        Seq(
          // jQuery
          Script from url("https://code.jquery.com/jquery-2.2.4.min.js"),
          // Boostrap
          Style from url(bootstrap % "css/bootstrap.css"),
          Script from url(bootstrap % "js/bootstrap.js"),
          Style from url("https://raw.githubusercontent.com/FortAwesome/Font-Awesome/v4.5.0/css/font-awesome.css"),
          // Video.js
          Script from url(VideoJSDist + "video.js"),
          Style from url(VideoJSDist + "video-js.css"),
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
          Image("favicon.ico").withMime("image/x-icon") from file("frontend/webapp/img/favicon.ico")
        )
      Bundle("index", jsDeps, appFiles, fonts, SJSApps.bundlerApp(frontend, fastOpt = false).value)
    },
    scalaJsBundlerCompilers := com.karasiq.scalajsbundler.compilers.AssetCompilers.keepJavaScriptAsIs
  ))

lazy val frontendSettings =
  Seq(
    name := "webm-tv-frontend",
    // resolvers                                 ++= Resolver.sonatypeOssRepos("snapshots"),
    libraryDependencies ++= Seq(
      "com.lihaoyi"        %%% "scalatags"         % "0.12.0",
      "com.lihaoyi"        %%% "scalarx"           % "0.4.3",
      "com.lihaoyi"        %%% "upickle"           % "2.0.0",
      "org.scala-js"       %%% "scalajs-dom"       % "1.0.0",
      "com.github.karasiq" %%% "scalajs-videojs"   % "1.1.1",
      "com.github.karasiq" %%% "scalajs-bootstrap" % "2.4.2"
    ),
    Compile / scalaJSUseMainModuleInitializer := true,
    Compile / npmDependencies ++= Seq(
      "video.js" -> "7.20.3"
    )
  )

lazy val dockerSettings =
  Seq(
    dockerBaseImage    := "openjdk:11-jre-stretch",
    dockerExposedPorts := Seq(8900),
    dockerUsername     := Some("karasiq"),
    dockerUpdateLatest := true
  )

// Projects
lazy val backend =
  project.in(file("."))
    .settings(commonSettings, backendSettings, dockerSettings)
    .enablePlugins(SJSAssetBundlerPlugin, JavaAppPackaging, DockerPlugin)

lazy val frontend =
  project.in(file("frontend"))
    .settings(commonSettings, frontendSettings)
    .enablePlugins(ScalaJSBundlerPlugin)
