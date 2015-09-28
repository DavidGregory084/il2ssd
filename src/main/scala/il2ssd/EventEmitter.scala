package il2ssd

import scala.scalajs.js

@js.native
trait EventEmitter extends js.Any {
  def on(event: String, listener: js.Function0[Unit]): EventEmitter = js.native
}