package il2ssd

import rx._

object State {
  val connection = Var(Option.empty[Connection])

  // UI States
  val connected: Rx[Boolean] = Rx { connection().nonEmpty }
  val loaded = Var(false)
  val loading = Var(false)
  val playing = Var(false)

  // Mission UI States
  val cycleRunning = Var(false)
  val dcgRunning = Var(false)
}