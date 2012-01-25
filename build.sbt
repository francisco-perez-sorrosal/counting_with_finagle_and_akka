import com.typesafe.startscript.StartScriptPlugin

seq(StartScriptPlugin.startScriptForClassesSettings: _*)

name := "counting_with_finagle_and_akka"

version := "0.1"

scalaVersion := "2.9.1"

resolvers += "twitter-repo" at "http://maven.twttr.com"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.twitter" % "finagle-core_2.9.1" % "1.9.12", 
  "com.twitter" % "finagle-http_2.9.1" % "1.9.12",
  "org.specs2" %% "specs2" % "1.6.1" % "test",
  "junit" % "junit" % "4.7" % "test",
  "se.scalablesolutions.akka" % "akka-actor" % "1.2",
  "se.scalablesolutions.akka" % "akka-remote" % "1.2",
  "se.scalablesolutions.akka" % "akka-testkit" % "1.2",
  "com.weiglewilczek.slf4s" %% "slf4s" % "1.0.7",
  "org.slf4j" % "slf4j-api" % "1.6.1",
  "ch.qos.logback" % "logback-core" % "0.9.28",
  "ch.qos.logback" % "logback-classic" % "0.9.28"
  )