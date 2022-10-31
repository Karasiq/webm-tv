logLevel := Level.Warn

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.33")

addSbtPlugin("com.github.karasiq" % "sbt-scalajs-bundler" % "1.0.7")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.2")

libraryDependencies += "com.lihaoyi" %% "scalatags" % "0.5.4"