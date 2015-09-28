package il2ssd

import scala.scalajs.js
import js.Dynamic.{ global => g }

@js.native
trait BrowserWindow extends js.Object with EventEmitter {
  def openDevTools(): Unit = js.native
  def loadUrl(url: String): Unit = js.native
}

object BrowserWindow {
  val browserWindow = g.require("browser-window")

  def apply(width: Int = 800, height: Int = 600): BrowserWindow = {
    js.Dynamic.newInstance(browserWindow)(
      js.Dynamic.literal(width = width, height = height)
    ).asInstanceOf[BrowserWindow]
  }
}