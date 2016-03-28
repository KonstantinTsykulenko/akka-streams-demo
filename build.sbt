name := "akka-streams-crawler"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream-experimental" % "2.0.3",
  "com.typesafe.akka" %% "akka-actor" % "2.4.2",
  "io.spray" %% "spray-client" % "1.3.2",
  "org.jsoup" % "jsoup" % "1.8.1"
)