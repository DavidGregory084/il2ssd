package il2ssd

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.io.Framing
import akka.stream.scaladsl.{ Concat, Keep, Flow, FlowGraph, Source, Tcp }
import akka.stream.stage.{ Context, PushStage, SyncDirective }
import akka.util.ByteString
import java.net.InetSocketAddress

object Daemon extends App {
  implicit val system = ActorSystem("system")
  implicit val materializer = ActorMaterializer()
  val address = new InetSocketAddress("ghserver", 21003)
  val connection = Tcp().outgoingConnection(address)

  val clientLogic = Flow() { implicit b =>
    import FlowGraph.Implicits._

    val replParser = new PushStage[String, ByteString] {
      override def onPush(elem: String, ctx: Context[ByteString]): SyncDirective = {
        elem match {
          case "exit" => ctx.finish()
          case _ => ctx.push(ByteString("$elem\n"))
        }
      }
    }

    val initialRequest = Source.single(ByteString("server\n"))
    val repl = b.add(Flow[ByteString]
      .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = Int.MaxValue, allowTruncation = false))
      .map(_.utf8String)
      .map(text => println(s"Received: $text"))
      .map(_ => readLine("> "))
      .transform(() => replParser))
    val concat = b.add(Concat[ByteString]())

    initialRequest ~> concat.in(0)
    repl.outlet ~> concat.in(1)

    (repl.inlet, concat.out)
  }

  connection.join(clientLogic).run()

  Thread.sleep(10000)
  system.shutdown()
}