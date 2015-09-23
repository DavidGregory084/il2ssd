lazy val commonSettings = Seq(
  name := "il2ssd",
  organization := "il2ssd",
  version := "2.0.0-SNAPSHOT",
  scalaVersion := "2.11.7"
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(scalariformSettings: _*).
  settings(
    fork := true,
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-unchecked"
    ),
    libraryDependencies ++= Seq(
      "org.scalafx" %% "scalafx" % "8.0.40-R8",
      "com.lihaoyi" %% "scalarx" % "0.2.8",
      "com.typesafe" % "config" % "1.3.0",
      "com.typesafe.akka" %% "akka-stream-experimental" % "1.0",
      "com.typesafe.akka" %% "akka-slf4j" % "2.3.13",
      "ch.qos.logback" % "logback-classic" % "1.1.3",
      "org.parboiled" %% "parboiled" % "2.1.0",
      "org.apache.commons" % "commons-lang3" % "3.4",
      "com.lihaoyi" %% "ammonite-ops" % "0.4.6",
      "org.scalatest" %% "scalatest" % "2.2.4" % "test",
      "org.scalacheck" %% "scalacheck" % "1.12.4" % "test"
    )
  )