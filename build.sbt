scalaVersion := "2.11.7"

scalacOptions += "-feature"

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"

resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"

resolvers += "Scalaz Bintray Repo" at "https://dl.bintray.com/scalaz/releases"

organization := "no.mesan"

name := "hipchatparse"

assemblyJarName in assembly := "hipchatparse.jar"

version := "0.1-SNAPSHOT"

libraryDependencies ++= Seq(
  "joda-time" % "joda-time" % "2.3",
  "org.joda" % "joda-convert" % "1.4",
  "org.scalaj" % "scalaj-time_2.10.2" % "0.7",
  "org.scalaz.stream" %% "scalaz-stream" % "0.7a",
  "com.typesafe.play" %% "play-json" % "2.4.2",
  "com.typesafe.akka" %% "akka-actor" % "2.3.12"
)

libraryDependencies ++= Seq( // test
  "junit" % "junit" % "4.11" % "test",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "org.hamcrest" % "hamcrest-core" % "1.1" % "test",
  "org.specs2" %% "specs2" % "3.3.1" % "test",
  "org.mockito" % "mockito-all" % "1.9.0" % "test"
)

// org.scalastyle.sbt.ScalastylePlugin.Settings
