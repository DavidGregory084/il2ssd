package il2ssd

import scalafx.collections._
import scalafx.geometry._
import scalafx.scene.control._
import scalafx.scene.layout._

object Views {
  def sceneRoot = new BorderPane {
    prefHeight = 528
    prefWidth = 500
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
      new Tab {
        text = "Pilots"
        content = pilotsTab
      },
      new Tab {
        text = "Ban List"
      },
      new Tab {
        text = "Mission"
      },
      new Tab {
        text = "Settings"
      }
    )
  }

  def toolBar = new ToolBar {
    content = List(
      toolBarButton("\uf090 Connect"),
      toolBarButton("\uf08b Disconnect"),
      new StackPane {
        prefHeight = 20
        hgrow = Priority.Always
        children = List(
          new ProgressIndicator {
            progress = -1
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
      prefHeight = 30
      prefWidth = 95
    }
  }

  def consoleTab = new BorderPane {
    padding = Insets(5)
    center = new BorderPane {
      center = new TextArea {
        editable = false
        focusTraversable = false
      }
    }
    bottom = new BorderPane {
      padding = Insets(5, 0, 0, 0)
      center = new TextField {
        prefHeight = 30
        promptText = "enter command"
      }
    }
  }

  def pilotsTab = new BorderPane {
    padding = Insets(5)
    center = new TableView {
      items = ObservableBuffer()
    }
  }
}