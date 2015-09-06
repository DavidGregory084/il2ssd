package il2ssd

import akka.actor.{ ActorRef, ActorSystem }
import akka.util.ByteString
import java.net.InetSocketAddress
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.text.Font

object App extends JFXApp {
  import Daemon._

  val fontAwesome = getClass.getResource("fontawesome-webfont.ttf")
  val styleSheet = getClass.getResource("main.css")
  var connection = None: Option[ActorRef]
  Font.loadFont(fontAwesome.toExternalForm, 12.0)

  override def stopApp() = {
    connection map { _ ! Close }
    Daemon.stop()
  }

  connection = Some(Daemon.start(new InetSocketAddress("ghserver", 21003)))

  connection map { _ ! ByteString("difficulty\n") }

  stage = new PrimaryStage {
    title = "Il-2 Simple Server Daemon"
    scene = new Scene {
      stylesheets = List(styleSheet.toExternalForm)
      root = Views.sceneRoot
    }
  }
}