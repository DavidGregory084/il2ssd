import java.nio.charset.Charset

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
    libraryDependencies += "com.lihaoyi" %%% "scalarx" % "0.2.8"
  )

enablePlugins(ScalaJSPlugin)

scalaJSStage in Global := FastOptStage

skip in packageJSDependencies := false

/* Use method of generating main.js from bchazalet/scalajs-electron-skeleton */
persistLauncher in Compile := true
persistLauncher in Test := false

val mainPath = SettingKey[File]("main-path", "The absolute path of main.js.")
val genMain = TaskKey[File]("gen-main", "Generate main.js.")

mainPath := { baseDirectory.value / "main.js" }

genMain := {
  val launcherCode = IO.read((packageScalaJSLauncher in Compile).value.data, Charset.forName("UTF-8"))
  val projectCode = IO.read((fastOptJS in Compile).value.data, Charset.forName("UTF-8"))
  val jsDependencies = (packageJSDependencies in Compile).value

  val electronGlobal = 
  """var addGlobalProps = function(obj) {
  |  obj.require = require;
  |  obj.__dirname = __dirname;
  |}
  |if((typeof __ScalaJSEnv === "object") && typeof __ScalaJSEnv.global === "object") {
  |  addGlobalProps(__ScalaJSEnv.global);
  |} else if(typeof  global === "object") {
  |  addGlobalProps(global);
  |} else if(typeof __ScalaJSEnv === "object") {
  |  __ScalaJSEnv.global = {};
  |  addGlobalProps(__ScalaJSEnv.global);
  |} else {
  |  var __ScalaJSEnv = { global: {} };
  |  addGlobalProps(__ScalaJSEnv.global)
  |}
  |""".stripMargin

  val destination = mainPath.value
  IO.write(destination, electronGlobal + projectCode + launcherCode, Charset.forName("UTF-8"))
  destination
}