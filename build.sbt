import Dependencies._

ThisBuild / scalaVersion     := "2.13.12"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "akka-scala",
    libraryDependencies += munit % Test,
    libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % "2.8.5",
    libraryDependencies += "com.typesafe.akka" %% "akka-actor-testkit-typed" % "2.8.5",
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.10"
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
