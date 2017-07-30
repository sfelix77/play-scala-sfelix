name := """play-scala-sfelix"""
organization := "fr.selix"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

resolvers += Resolver.sonatypeRepo("snapshots")

scalaVersion := "2.12.2"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.0" % Test

libraryDependencies += "org.jsr107.ri" % "cache-annotations-ri-guice" % "1.0.0"
libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.3.4"