package il2ssd

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props, Stash }
import akka.io.{ IO, Tcp }
import akka.util.ByteString
import java.net.InetSocketAddress
import org.apache.commons.lang3.StringEscapeUtils

object Daemon {
  trait Command
  case object Close extends Command

  private lazy val actorSystem = ActorSystem("system")

  def start(address: InetSocketAddress) = actorSystem.actorOf(Daemon.props(address), "daemon")
  def stop() = actorSystem.shutdown()

  def props(address: InetSocketAddress) = Props(classOf[Daemon], address)
}

class Daemon(address: InetSocketAddress) extends Actor with ActorLogging with Stash {
  import Tcp._
  import context.system

  IO(Tcp) ! Connect(address)

  def tcpClient(connection: ActorRef): Receive = {
    case data: ByteString =>
      log.debug(s"Write: ${StringEscapeUtils.escapeJava(data.utf8String)}")
      connection ! Write(data)
    case CommandFailed(_: Write) =>
      log.error("CommandFailed: Write")
    case Received(data) =>
      log.debug(s"Received: '${StringEscapeUtils.escapeJava(data.utf8String)}'")
    case Daemon.Close =>
      log.debug("Stop")
      connection ! Close
    case _: ConnectionClosed =>
      log.debug("ConnectionClosed")
      context stop self
  }

  def receive = {
    case CommandFailed(_: Connect) =>
      log.error("CommandFailed: Connect")
      context stop self
    case _: Connected =>
      log.info("Connected")
      val connection = sender()
      connection ! Register(self)
      unstashAll
      context become tcpClient(connection)
    case _ => stash()
  }
}