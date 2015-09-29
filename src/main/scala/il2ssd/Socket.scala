package il2ssd

import scala.scalajs.js
import js.annotation.JSName
import js.Dynamic.{ global => g }

@js.native
@JSName("net.Socket")
class Socket extends js.Object with EventEmitter {
  def write(str: String, cb: js.Function = ???): Boolean = js.native
  def connect(port: Double, host: String, connectionListener: js.Function = ???): Unit = js.native
  def end(): Unit = js.native
}