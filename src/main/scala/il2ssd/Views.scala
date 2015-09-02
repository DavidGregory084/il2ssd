package il2ssd

import scalafx.geometry.Insets
import scalafx.scene.control._
import scalafx.scene.layout._

object Views {
  def sceneRoot = new BorderPane {
    prefHeight = 528.0
    prefWidth = 500.0
    top = menuBar
    center = tabBar
    bottom = toolBar
  }

  def menuBar = new MenuBar {
    menus = List(
      new Menu("File") { items = List(new MenuItem("Exit")) },
      new Menu("Help") { items = List(new MenuItem("About")) }
    )
  }

  def tabBar = new TabPane {
    tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
    tabs = List(
      new Tab {
        text = "Console"
        content = consoleTab
      },
      new Tab { text = "Pilots" },
      new Tab { text = "Ban List" },
      new Tab { text = "Mission" },
      new Tab { text = "Settings" }
    )
  }

  def toolBar = new ToolBar {
    content = List(
      toolBarButton("\uf090 Connect"),
      toolBarButton("\uf08b Disconnect"),
      new StackPane {
        hgrow = Priority.Always
        children = List(
          new ProgressIndicator {
            progress = -1.0
            visible = false
          }
        )
      },
      toolBarButton("\uf04b\uf021 Start"),
      toolBarButton("\uf04e Next")
    )
  }

  def toolBarButton(text: String) = {
    new Button(text) {
      prefHeight = 30.0
      prefWidth = 95.0
    }
  }

  def consoleTab = new BorderPane {
    prefHeight = 419.0
    center = new BorderPane {
      padding = Insets(10.0, 10.0, 5.0, 10.0)
      center = new TextArea {
        editable = false
        focusTraversable = false
      }
    }
    bottom = new BorderPane {
      padding = Insets(5.0, 10.0, 0.0, 10.0)
      center = new TextField {
        prefHeight = 30.0
        promptText = "enter command"
      }
    }
  }
}