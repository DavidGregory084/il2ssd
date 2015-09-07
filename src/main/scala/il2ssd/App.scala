package il2ssd

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.text.Font

object App extends JFXApp {
  import Daemon._

  val fontAwesome = getClass.getResource("fontawesome-webfont.ttf")
  val styleSheet = getClass.getResource("main.css")
  Font.loadFont(fontAwesome.toExternalForm, 12.0)

  stage = new PrimaryStage {
    title = "Il-2 Simple Server Daemon"
    scene = new Scene {
      stylesheets = List(styleSheet.toExternalForm)
      root = Views.sceneRoot
    }
  }
}