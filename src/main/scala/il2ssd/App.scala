package il2ssd

import scala.scalajs.js
import js.Dynamic.{ global => g }

object App extends js.JSApp {
  def main(): Unit = {
    val app = g.require("app")
    val browserWindow = g.require("browser-window")

    g.require("crash-reporter").start()

    var mainWindow: js.Dynamic = null

    app.on("window-all-closed", () => app.quit())

    app.on("ready", () => {
      mainWindow = js.Dynamic.newInstance(browserWindow)(js.Dynamic.literal(width = 800, height = 600))

      mainWindow.loadUrl("file://" + g.__dirname + "/index.html")

      mainWindow.openDevTools()

      mainWindow.on("closed", () => mainWindow = null)
    })
  }
}