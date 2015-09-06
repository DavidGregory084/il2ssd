package il2ssd

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props, Stash }
import akka.io.{ IO, Tcp }
import akka.util.ByteString
import java.net.InetSocketAddress
import org.apache.commons.lang3.StringEscapeUtils

object Daemon extends App {
  case object Stop
  val system = ActorSystem("system")
  val daemon = system.actorOf(Props(classOf[Daemon], new InetSocketAddress("ghserver", 21003)), "daemon")

  daemon ! ByteString("server\n")

  Thread.sleep(2000)

  daemon ! Stop

  system.shutdown()
}

class Daemon(address: InetSocketAddress) extends Actor with ActorLogging with Stash {
  import Daemon._
  import Tcp._
  import context.system

  IO(Tcp) ! Connect(address)

  def tcpClient(connection: ActorRef): Receive = {
    case data: ByteString =>
      log.debug(s"Write: ${StringEscapeUtils.escapeJava(data.utf8String)}")
      connection ! Write(data)
    case CommandFailed(w: Write) =>
      log.error("failed to write!")
    case Received(data) =>
      log.debug(s"Received: '${StringEscapeUtils.escapeJava(data.utf8String)}'")
    case Stop =>
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