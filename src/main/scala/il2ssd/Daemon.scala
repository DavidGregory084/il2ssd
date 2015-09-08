package il2ssd

import akka.actor.{ ActorRef, ActorSystem }
import akka.stream.{ ActorMaterializer, OverflowStrategy }
import akka.stream.io.Framing
import akka.stream.scaladsl.{ Sink, Keep, Flow, FlowGraph, Source, Tcp }
import akka.stream.scaladsl.FlowGraph.Builder
import akka.stream.stage.{ Context, PushStage, SyncDirective }
import akka.util.ByteString
import java.net.InetSocketAddress
import org.apache.commons.lang3.StringEscapeUtils

object Daemon extends App {
  implicit val system = ActorSystem("system")
  implicit val materializer = ActorMaterializer()
  val address = new InetSocketAddress("ghserver", 21003)
  val connection = Tcp().outgoingConnection(address)

  val commandSource = Source.actorRef(
    bufferSize = 10, 
    overflowStrategy = OverflowStrategy.dropNew
  )

  val serverOutputFlow = Flow[ByteString]
    .via(Framing.delimiter(ByteString("\r\n"), maximumFrameLength = Int.MaxValue, allowTruncation = false))
    .filter(s => !s.startsWith(ByteString("<consoleN>")))
    .map(s => StringEscapeUtils.unescapeJava(s.utf8String))
    .to(Sink.foreach(s => print(s"Received: $s")))

  val clientLogic = Flow(commandSource) {
    implicit builder => commandInput =>
      import FlowGraph.Implicits._
      val serverOutput = builder.add(serverOutputFlow)
      (serverOutput.inlet, commandInput.outlet)
  }

  val commandStream = connection.joinMat(clientLogic)(Keep.right).run()

  commandStream ! ByteString("difficulty\n")
  commandStream ! ByteString("server\n")

  Thread.sleep(10000)
  system.shutdown()
}