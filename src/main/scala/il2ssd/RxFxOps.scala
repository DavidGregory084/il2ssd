package il2ssd

import rx._
import javafx.beans.value.{ ObservableValue, ChangeListener }

/**
 *  Scala.Rx integration, mostly taken from Eugen Kiss' [[https://github.com/eugenkiss/7guis 7guis]]
 */
object RxFxOps {
  val observers: scala.collection.mutable.Buffer[Obs] = scala.collection.mutable.Buffer()

  implicit class PropertyExtensions[T, J](p: scalafx.beans.property.Property[T, J]) {
    def |=(x: => T) = {
      val rx = Rx { x }
      observers += Obs(rx) { p() = rx() }
    }

    def |=(q: scalafx.beans.property.Property[T, _]) {
      val rx = q.rx()
      observers += Obs(rx) { p() = rx() }
    }
  }

  implicit class ReadOnlyPropertyExtensions[T, J](p: scalafx.beans.property.ReadOnlyProperty[T, J]) {
    def rx(): Rx[T] = {
      val v = Var(p.value)
      p.addListener(new ChangeListener[J] {
        override def changed(observable: ObservableValue[_ <: J], oldValue: J, newValue: J): Unit = {
          v.update(p.value)
        }
      })
      v
    }
  }

  implicit class ReadOnlyPropertyExtensionsJ[T](p: javafx.beans.property.ReadOnlyProperty[T]) {
    def rx(): Rx[T] = {
      val v = Var(p.getValue)
      p.addListener(new ChangeListener[T] {
        override def changed(observable: ObservableValue[_ <: T], oldValue: T, newValue: T): Unit = {
          v.update(p.getValue)
        }
      })
      v
    }
  }
}