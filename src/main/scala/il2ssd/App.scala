package il2ssd

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import java.net.InetSocketAddress
import rx._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.text.Font
import scalafx.Includes._

object App extends JFXApp {
  implicit val config = ConfigFactory.load("il2ssd")
  implicit val system = ActorSystem("system")
  implicit val materializer = ActorMaterializer()

  override def stopApp() { system.shutdown() }

  val fontAwesome = getClass.getResource("fontawesome-webfont.ttf")
  val styleSheet = getClass.getResource("main.css")
  Font.loadFont(fontAwesome.toExternalForm, 12.0)

  State.connection() = Some(new Connection(new InetSocketAddress("ghserver", 21003)))
  stage = new PrimaryStage {
    title = "Il-2 Simple Server Daemon"
    scene = new Scene {
      stylesheets = List(styleSheet.toExternalForm)
      root = Views.sceneRoot
    }
  }
}