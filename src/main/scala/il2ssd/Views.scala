package il2ssd

import rx._
import rx.ops._
import RxFxOps._
import scalafx.application._
import scalafx.collections._
import scalafx.event._
import scalafx.geometry._
import scalafx.scene.control._
import scalafx.scene.input._
import scalafx.scene.layout._
import scalafx.Includes._

object Views {
  lazy val sceneRoot = new BorderPane {
    prefHeight = 528
    prefWidth = 500
    top = menuBar
    center = tabBar
    bottom = toolBar
  }

  lazy val menuBar = new MenuBar {
    menus = List(
      new Menu("File") {
        items = List(
          new MenuItem("Exit") {
            onAction = (_: ActionEvent) => Platform.exit()
          }
        )
      },
      new Menu("Help") { items = List(new MenuItem("About")) }
    )
  }

  lazy val tabBar = new TabPane {
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

  lazy val toolBar = new ToolBar {
    content = List(
      new Button("\uf090 Connect") {
        prefHeight = 30
        prefWidth = 95
        disable |= State.connected()
      },
      new Button("\uf08b Disconnect") {
        prefHeight = 30
        prefWidth = 95
        disable |= !State.connected()
      },
      new StackPane {
        prefHeight = 20
        hgrow = Priority.Always
        children = List(
          new ProgressIndicator {
            progress = -1
            visible = State.loading()
          }
        )
      },
      new Button("\uf04b\uf021 Start") {
        prefHeight = 30
        prefWidth = 95
        disable |= Rx { !State.connected() || !State.loaded() }()
      },
      new Button("\uf04e Next") {
        prefHeight = 30
        prefWidth = 95
        disable = true
      }
    )
  }

  lazy val consoleTab = new BorderPane {
    padding = Insets(5)
    center = new BorderPane {
      center = consoleTextArea
    }
    bottom = new BorderPane {
      padding = Insets(5, 0, 0, 0)
      center = commandEntryField
    }
  }

  lazy val consoleTextArea = new TextArea {
    promptText = "<disconnected>"
    editable = false
    focusTraversable = false
    text |= State.received.reduce { (current, add) =>
      val dropped =
        if (current.count(_ == '\n') > 999)
          current.dropWhile(_ != '\n')
        else current
      dropped ++ add
    }()

    text.onInvalidate {
      this.scrollTop = Double.MaxValue
    }
  }

  lazy val commandEntryField = new TextField {
    prefHeight = 30
    promptText = "enter command"
    onKeyPressed = (ev: KeyEvent) => ev match {
      case EnterKeyPressed() =>
        this.text.value match {
          case "clear" =>
            State.received() = ""
            consoleTextArea.clear
            this.clear
          case command =>
            State.connection().map(_.send(command))
            this.clear
        }
      case _ =>
    }
  }

  lazy val pilotsTab = new BorderPane {
    padding = Insets(5)
    center = new TableView {
      items = ObservableBuffer()
    }
  }
}