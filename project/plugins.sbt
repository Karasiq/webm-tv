logLevel := Level.Warn

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.9")

addSbtPlugin("com.github.karasiq" % "sbt-scalajs-bundler" % "1.0.5")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.0.6")

libraryDependencies += "com.lihaoyi" %% "scalatags" % "0.5.3"