import scalafx.scene.control._
import scalafx.scene.input._

package object il2ssd {

  object EnterKeyPressed {
    def unapply(ev: KeyEvent) = ev.code == KeyCode.ENTER
  }

  implicit class TextInputOps(ti: TextInputControl) {
    def clear() = ti.text = ""
  }

}