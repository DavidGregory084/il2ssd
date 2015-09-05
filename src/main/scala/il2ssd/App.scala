package il2ssd

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.text.Font

object App extends JFXApp {
  Font.loadFont(getClass.getResource("fontawesome-webfont.ttf").toExternalForm, 12.0)
  stage = new PrimaryStage {
    title = "Il-2 Simple Server Daemon"
    scene = new Scene {
      stylesheets = List(getClass.getResource("main.css").toExternalForm)
      root = Views.sceneRoot
    }
  }
}