name := "NFAbric"
version := "1.0"
scalaVersion := "2.13.8"

organization := "com.nfabric"
ThisBuild / version := "0.1.0-SNAPSHOT"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "utf-8",
  "-feature",
  "-language:implicitConversions",
  "-unchecked",
  "-Xlint",
  "-Ywarn-dead-code",
  "-Ywarn-unused",
  "-Ywarn-unused-import"
)

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1",
  "org.scalatest" %% "scalatest" % "3.2.15" % Test,
  "org.scalacheck" %% "scalacheck" % "1.17.0" % Test
)

Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oD")

// Enable caching for faster builds
ThisBuild / turbo := true

// Add a resolver for additional libraries
resolvers += Resolver.sonatypeRepo("public")

// Add sbt-updates plugin for dependency updates
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.5.3")

// Add sbt-scalafmt plugin for code formatting
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")