package il2ssd

import java.net.InetSocketAddress
import rx._
import rx.ops._

object State {
  // Application State
  val connection = Var(Option.empty[Connection])
  val received = Var("")

  // UI State
  val connected = Rx { connection().nonEmpty }
  val loaded = Var(false)
  val loading = Var(false)
  val playing = Var(false)

  // Mission UI State
  val cycleRunning = Var(false)
  val dcgRunning = Var(false)
}