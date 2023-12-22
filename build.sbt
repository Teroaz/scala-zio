val zioVersion = "2.0.20"

val scala3Version = "3.3.1"

ThisBuild / organization := "fr.efrei"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := scala3Version

lazy val root = project
  .in(file("."))
  .settings(
    name := "my-project",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-streams" % zioVersion,
      "dev.zio" %% "zio-http" % "3.0.0-RC3",
      "io.github.cdimascio" % "dotenv-java" % "3.0.0"
    ),
    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test
  )