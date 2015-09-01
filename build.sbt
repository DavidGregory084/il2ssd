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
      "org.scalatest" %% "scalatest" % "2.2.4" % "test",
      "org.scalacheck" %% "scalacheck" % "1.12.4" % "test"
    )
  )