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

class Connection(address: InetSocketAddress)(implicit system: ActorSystem, materializer: ActorMaterializer) {
  private val connection = Tcp().outgoingConnection(address)

  private val commandSource = Source.actorRef(
    bufferSize = 10,
    overflowStrategy = OverflowStrategy.dropNew
  )

  private val serverOutputFlow = Flow[ByteString]
    .via(Framing.delimiter(ByteString("\r\n"), maximumFrameLength = Int.MaxValue, allowTruncation = false))
    .filter(s => !s.startsWith(ByteString("<consoleN>")))
    .map(s => StringEscapeUtils.unescapeJava(s.utf8String))
    .to(Sink.foreach(s => State.received() = State.received() ++ s))

  private val clientLogic = Flow(commandSource) { implicit builder => commandInput =>
    import FlowGraph.Implicits._
    val serverOutput = builder.add(serverOutputFlow)
    (serverOutput.inlet, commandInput.outlet)
  }

  private val commandStream = connection.joinMat(clientLogic)(Keep.right).run()

  def send(message: String): Unit = commandStream ! ByteString(message + "\n")
}