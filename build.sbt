lazy val commonSettings = Seq(
  name := "il2ssd",
  organization := "il2ssd",
  version := "0.4.0-SNAPSHOT",
  scalaVersion := "2.11.7"
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(scalariformSettings: _*).
  settings(
    jsDependencies ++= Seq(
      RuntimeDOM
    )
  )

enablePlugins(ScalaJSPlugin)

scalaJSStage in Global := FastOptStage