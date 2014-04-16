;;
;; ## UI initialisation functions
;;
;; In this namespace we define the functions which instantiate any objects that
;; we need and initialise the objects we receive by dependency injection into data
;; structures which we can more easily manipulate in Clojure.
(ns uk.org.il2ssd.jfx.init
  (:require [clojure.java.io :refer [resource]]
            [uk.org.il2ssd.event.console :as console]
            [uk.org.il2ssd.event.cycle :as cycle]
            [uk.org.il2ssd.event.main :as main]
            [uk.org.il2ssd.event.mission :as mission]
            [uk.org.il2ssd.event.settings :as settings]
            [uk.org.il2ssd.jfx.ui :as ui]
            [uk.org.il2ssd.jfx.util :as util]
            [uk.org.il2ssd.config :refer [get-configuration read-config-file]]
            [uk.org.il2ssd.state :as state])
  (:import (java.net URL)
           (java.nio.file Path Paths)
           (java.util List)
           (javafx.collections FXCollections)
           (javafx.scene Scene)
           (javafx.scene.control Button ChoiceBox Label MenuItem SelectionModel
                                 TableColumn TableView Tab TextField)
           (javafx.scene.control.cell PropertyValueFactory
                                      TextFieldTableCell ChoiceBoxTableCell)
           (javafx.scene.layout BorderPane HBox Priority Region StackPane)
           (javafx.scene.text Font)
           (javafx.stage FileChooser FileChooser$ExtensionFilter Stage)
           (uk.org.il2ssd.jfx ConsolePresenter ConsoleView CyclePresenter
                              CycleView MainPresenter MainView
                              SettingsPresenter SettingsView
                              SinglePresenter SingleView)))

(def modes
  "### modes
   This is a map of the mission loading modes."
  {:single "Single Mission", :cycle "Mission Cycle"})

(defn init-stage
  "### init-stage
   This zero argument function instantiates the subclasses of afterburner.fx's
   FXMLView class that we have defined to hold our views.

   It then sets the title, sets the scene for this stage to a new Scene object
   which contains the main view and defines the stage as non-resizable before
   showing the stage.

   It also loads the icon font that we will use for the UI and sets an event
   handler function to run when the user requests to close the stage.

   Finally, it gets the presenter instance for each of these views and loads
   them into a map atom."
  []
  (let [^Stage stage @state/stage
        main-view (MainView.)
        console-view (ConsoleView.)
        single-view (SingleView.)
        cycle-view (CycleView.)
        settings-view (SettingsView.)
        scene (Scene. (.getView main-view))
        main-presenter (.getPresenter main-view)
        console-presenter (.getPresenter console-view)
        single-presenter (.getPresenter single-view)
        cycle-presenter (.getPresenter cycle-view)
        settings-presenter (.getPresenter settings-view)]
    (main/set-title)
    (doto stage
      (.setScene scene)
      (.setResizable false)
      (.show))
    (Font/loadFont
      (.toExternalForm ^URL (resource "fontawesome-webfont.ttf")) 12.0)
    (util/close-handler stage main/close)
    (reset! state/presenters
            {:main-presenter     main-presenter
             :console-presenter  console-presenter
             :single-presenter   single-presenter
             :cycle-presenter    cycle-presenter
             :settings-presenter settings-presenter})))

(defn init-objects
  "### init-objects
   This one argument function retrieves the presenter instances from our presenter
   atom before retrieving the objects from these presenters and loading them into
   a map which we put into a global state atom.

   By doing this, we can refer to an object instance in any namespace by using the
   following syntax:

       (let [{:keys [<objects we want to use>]}
             @state/controls]
         (<function body>))

   It may be necessary to use type hinting to limit uses of the Reflection API at
   runtime for performance reasons; Clojure can't simply infer what you're going
   to hold in an atom at runtime."
  []
  (let [{:keys [^MainPresenter main-presenter
                ^ConsolePresenter console-presenter
                ^SinglePresenter single-presenter
                ^CyclePresenter cycle-presenter
                ^SettingsPresenter settings-presenter]} @state/presenters]
    (reset! state/controls
            {;Main FXML file controls
              :connect-btn       (.getConnectButton main-presenter)
              :disconn-btn       (.getDisconnectButton main-presenter)
              :prog-stack        (.getProgressStack main-presenter)
              :prog-ind          (.getProgressIndicator main-presenter)
              :start-btn         (.getStartStopButton main-presenter)
              :next-btn          (.getNextButton main-presenter)
              :console-tab       (.getConsoleTab main-presenter)
              :settings-tab      (.getSettingsTab main-presenter)
              :mission-pane      (.getMissionPane main-presenter)
              :mode-choice       (.getMissionModeChoice main-presenter)
              :mission-spring    (.getMissionBarSpring main-presenter)
              :load-btn          (.getMissionLoadButton main-presenter)
              :exit-btn          (.getExitItem main-presenter)
              :about-btn         (.getAboutItem main-presenter)
              :mis-chooser       (FileChooser.)
              :dcg-chooser       (FileChooser.)
             ;Console Tab FXML file controls
              :console-pane      (.getConsolePane console-presenter)
              :cmd-entry         (.getCommandEntryField console-presenter)
              :console           (.getConsoleTextArea console-presenter)
             ;Single Mission FXML file controls
              :single-mis-pane   (.getSingleMisPane single-presenter)
              :single-path-btn   (.getChooseSingleMisButton single-presenter)
              :single-path-fld   (.getSingleMisPathField single-presenter)
              :single-remote-btn (.getRemoteSelectButton single-presenter)
              :single-path-lbl   (.getSingleMisPathLabel single-presenter)
             ;Mission Cycle FXML file controls
              :cycle-mis-pane    (.getCycleMisPane cycle-presenter)
              :cycle-table       (.getCycleMissionTable cycle-presenter)
              :cycle-data        (FXCollections/synchronizedObservableList (FXCollections/observableArrayList))
              :cycle-mis-col     (.getCycleMissionColumn cycle-presenter)
              :cycle-tim-col     (.getCycleTimerColumn cycle-presenter)
              :cycle-mis-upbtn   (.getMissionUpButton cycle-presenter)
              :cycle-mis-delbtn  (.getMissionDeleteButton cycle-presenter)
              :cycle-mis-dwnbtn  (.getMissionDownButton cycle-presenter)
              :cycle-path-fld    (.getCycleMisPathField cycle-presenter)
              :cycle-path-btn    (.getChooseCycleMisButton cycle-presenter)
              :cycle-mis-addbtn  (.getAddMissionButton cycle-presenter)
             ;Settings Tab FXML file controls
              :settings-pane     (.getSettingsPane settings-presenter)
              :ip-field          (.getIpAddressField settings-presenter)
              :port-field        (.getPortField settings-presenter)
              :server-path-lbl   (.getServerPathLabel settings-presenter)
              :server-path-btn   (.getServerPathButton settings-presenter)
              :server-chooser    (FileChooser.)
              :get-diff-btn      (.getGetDifficultyButton settings-presenter)
              :set-diff-btn      (.getSetDifficultyButton settings-presenter)
              :diff-table        (.getDifficultyTable settings-presenter)
              :diff-data         (FXCollections/synchronizedObservableList (FXCollections/observableArrayList))
              :diff-set-col      (.getDiffSettingColumn settings-presenter)
              :diff-val-col      (.getDiffValueColumn settings-presenter)})))

(defn init-handlers
  "### init-handlers
   This zero argument function is used to add event handlers and change listeners
   to any objects that must respond to user input. We also add watch functions to
   the global state atoms where those state atoms' values should be reflected in
   the UI."
  []
  (let [{:keys [^Button connect-btn
                ^Button disconn-btn
                ^Button start-btn
                ^Button next-btn
                ^TextField cmd-entry
                ^ChoiceBox mode-choice
                ^Button load-btn
                ^MenuItem exit-btn
                ^MenuItem about-btn
                ^Label server-path-lbl
                ^Button server-path-btn
                ^Button get-diff-btn
                ^Button set-diff-btn
                ^TextField ip-field
                ^TextField port-field
                ^Button single-remote-btn
                ^Button single-path-btn
                ^Label single-path-lbl
                ^Button cycle-mis-upbtn
                ^Button cycle-mis-delbtn
                ^Button cycle-mis-dwnbtn
                ^Button cycle-mis-addbtn
                ^Button cycle-path-btn]}
        @state/controls]
    ;State atom watch functions
    (add-watch state/connected :connect main/set-connected)
    (add-watch state/loaded :load main/set-mission-loaded)
    (add-watch state/playing :play main/set-mission-playing)
    (add-watch state/mission-path :mis mission/set-mis-selected)
    (add-watch state/server-path :path settings/set-server-selected)
    ;Main UI EventHandlers and Listeners
    (util/button-handler connect-btn main/connect-command)
    (util/button-handler disconn-btn main/disconnect-command)
    (util/button-handler start-btn main/start-stop-command)
    (util/button-handler load-btn main/load-unload-command)
    (util/button-handler exit-btn main/close)
    ;Console tab
    (util/keypress-handler cmd-entry "Enter" console/enter-command)
    ;Mission tab
    (util/value-listener mode-choice mission/mode-choice modes)
    ;Single mission pane
    (util/button-handler single-remote-btn mission/set-single-remote)
    (util/button-handler single-path-btn mission/single-choose-command)
    (util/text-listener single-path-lbl mission/single-path-select)
    ;Mission cycle pane
    (util/button-handler cycle-mis-upbtn cycle/mission-swap dec)
    (util/button-handler cycle-mis-delbtn cycle/mission-delete)
    (util/button-handler cycle-mis-dwnbtn cycle/mission-swap inc)
    (util/button-handler cycle-mis-addbtn cycle/mission-add)
    (util/button-handler cycle-path-btn cycle/cycle-choose-command)
    ;Settings tab
    (util/button-handler server-path-btn settings/server-choose-command)
    (util/text-listener server-path-lbl settings/server-path-select)
    (util/button-handler get-diff-btn settings/get-difficulties)
    (util/button-handler set-diff-btn settings/set-difficulties)))

(defn init-controls
  "### init-controls
   The zero argument function is used to initialise any controls with default
   values. If a configuration file is found, the UI is initialised using the
   values retrieved from this file.

   We also add any content from our subsidiary FXML files.

   We also set the HGrow setting for some UI elements because this cannot be set
   within a ToolBar in the JavaFX Scene Builder (even though a ToolBar is a
   subclass of HBox)."
  []
  (let [{:keys [^Tab console-tab
                ^Tab settings-tab
                ^BorderPane console-pane
                ^BorderPane settings-pane
                ^TextField ip-field
                ^TextField port-field
                ^Label server-path-lbl
                ^Label single-path-lbl
                ^StackPane prog-stack
                ^ChoiceBox mode-choice
                ^Region mission-spring]} @state/controls
        config (-> (read-config-file) (get-configuration))
        ip (:ip-field config)
        port (:port-field config)
        srv-path (:server-path-lbl config)
        mode (-> (:mode-choice config) keyword modes)
        single-mis (:single-path-lbl config)]
    (HBox/setHgrow prog-stack Priority/ALWAYS)
    (HBox/setHgrow mission-spring Priority/ALWAYS)
    (.setContent console-tab console-pane)
    (.setContent settings-tab settings-pane)
    (-> mode-choice .getItems (.addAll ^List (map modes [:single :cycle])))
    (if config
      (do (-> mode-choice .getSelectionModel (.select mode))
          (.setText ip-field ip)
          (.setText port-field port)
          (.setText server-path-lbl srv-path)
          (.setText single-path-lbl single-mis))
      (-> mode-choice .getSelectionModel .selectFirst))))

(defn init-choosers
  "### init-choosers
   This zero argument function initialises the FileChooser objects that will be
   opened from the UI, setting the title, initial directory and file extension
   filter to be used.

   The initial directory for the mission file chooser is not set, as this will
   be resolved relative to the chosen Il-2 server path."
  []
  (let [{:keys [^FileChooser server-chooser
                ^FileChooser mis-chooser
                ^FileChooser dcg-chooser]} @state/controls]
    (doto server-chooser
      (.setTitle "Choose Il-2 Server Executable")
      (.setInitialDirectory
        (-> (Paths/get "" ^"[Ljava.lang.String;"
                       (into-array String []))
            .toAbsolutePath .toFile))
      (-> ^List .getExtensionFilters
          (.add
            (FileChooser$ExtensionFilter.
              "Il-2 Server (il2server.exe)"
              ^"[Ljava.lang.String;"
              (into-array String ["il2server.exe"])))))
    (doto mis-chooser
      (.setTitle "Choose Il-2 Mission File")
      (-> ^List .getExtensionFilters
          (.add
            (FileChooser$ExtensionFilter.
              "Il-2 Mission (*.mis)"
              ^"[Ljava.lang.String;"
              (into-array ["*.mis"])))))
    (doto dcg-chooser
      (.setTitle "Choose DCG Executable")
      (.setInitialDirectory
        (-> (Paths/get "" ^"[Ljava.lang.String;"
                       (into-array String []))
            .toAbsolutePath .toFile))
      (-> ^List .getExtensionFilters
          (.add
            (FileChooser$ExtensionFilter.
              "DCG Executable (il2dcg.exe)"
              ^"[Ljava.lang.String;"
              (into-array ["il2dcg.exe"])))))))

(defn init-diff-table
  "### init-diff-table
   This is a zero argument function which instantiates the cell factories and cell
   value factories for the difficulty table so that the table is populated
   correctly. The property which backs each column is defined in the constructor
   for the PropertyValueFactory for that column.

   We also define the CellFactory for the difficulty value column as
   TextFieldTableCell, which produces editable table cells.

   The backing list for the table is instantiated and stored in the controls
   atom before being linked to the table.

   Finally, we attach an EventHandler to the cell edit commit action which rejects
   any inputs which are not equal to 0 or 1, as these are the permitted values for
   Il-2 difficulty settings."
  []
  (let [{:keys [^TableView diff-table
                ^List diff-data
                ^TableColumn diff-set-col
                ^TableColumn diff-val-col]} @state/controls]
    (.setCellValueFactory diff-set-col (PropertyValueFactory. "setting"))
    (doto diff-val-col
      (.setCellFactory
        (ChoiceBoxTableCell/forTableColumn
          (FXCollections/observableArrayList (into-array String ["0" "1"]))))
      (.setCellValueFactory (PropertyValueFactory. "value"))
      (.setOnEditCommit (ui/diff-val-commit)))
    (doto diff-table
      (.setColumnResizePolicy TableView/CONSTRAINED_RESIZE_POLICY)
      (.setItems diff-data))))

(defn init-cycle-table
  "### init-cycle-table
 This is a zero argument function which instantiates the cell factories and cell
 value factories for the mission cycle table so that the table is populated
 correctly. The property which backs each column is defined in the constructor
 for the PropertyValueFactory for that column.

 We also define the CellFactory for the mission timer column as
 TextFieldTableCell, which produces editable table cells.

 The backing list for the table is instantiated and stored in the controls atom
 before being linked to the table.

 Finally, we attach an EventHandler to the cell edit commit action which rejects
 inputs which cannot be converted to an Integer or which are not greater than
 zero."
  []
  (let [{:keys [^TableView cycle-table
                ^List cycle-data
                ^TableColumn cycle-mis-col
                ^TableColumn cycle-tim-col]} @state/controls]
    (.setCellValueFactory cycle-mis-col (PropertyValueFactory. "mission"))
    (doto cycle-tim-col
      (.setCellFactory (TextFieldTableCell/forTableColumn))
      (.setCellValueFactory (PropertyValueFactory. "timer"))
      (.setOnEditCommit (ui/cycle-timer-commit)))
    (doto cycle-table
      (.setColumnResizePolicy TableView/CONSTRAINED_RESIZE_POLICY)
      (.setItems cycle-data))))