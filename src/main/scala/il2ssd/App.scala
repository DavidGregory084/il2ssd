package il2ssd

import scala.scalajs.js

@js.native
trait App extends js.Object with EventEmitter {
  def quit(): Unit = js.native
}