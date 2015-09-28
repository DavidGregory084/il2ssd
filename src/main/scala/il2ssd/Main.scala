package il2ssd

import scala.scalajs.js
import js.Dynamic.{ global => g }

object Main extends js.JSApp {
  def main(): Unit = {
    val app = g.require("app").asInstanceOf[App]

    var mainWindow: BrowserWindow = null

    app.on("window-all-closed", () => app.quit())

    app.on("ready", () => {
      mainWindow = BrowserWindow(width = 800, height = 600)

      mainWindow.on("closed", () => mainWindow = null)

      mainWindow.loadUrl("file://" + g.__dirname + "/index.html")

      mainWindow.openDevTools()
    })
  }
}