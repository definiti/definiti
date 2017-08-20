organization := "definiti"

name := "definiti"

version := "0.0.0"

scalaVersion := "2.12.1"

libraryDependencies += "com.typesafe" % "config" % "1.3.1"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.25"
libraryDependencies += "com.github.scopt" %% "scopt" % "3.6.0"
libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.0.8"
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.8"
libraryDependencies += "com.github.pathikrit" %% "better-files" % "3.0.0"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"
libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.13.5" % "test"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-language:implicitConversions", "-feature")

publishTo := Some(Resolver.mavenLocal)