ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "itriumTestProject"
  )

libraryDependencies+="com.lihaoyi" %% "requests" % "0.7.1"
libraryDependencies+="org.json4s" %% "json4s-jackson" % "4.0.5"
