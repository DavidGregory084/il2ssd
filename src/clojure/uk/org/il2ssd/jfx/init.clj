;;
;; ## UI initialisation functions
;;
;; In this namespace we define the functions which instantiate any objects that
;; we need and initialise the objects we retrieve using afterburner.fx into data
;; structures which we can more easily manipulate in Clojure.
(ns uk.org.il2ssd.jfx.init
  (:require [clojure.java.io :refer [resource]]
            [clojure.edn :as edn]
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
                                      TextFieldTableCell ChoiceBoxTableCell
                                      ComboBoxTableCell)
           (javafx.scene.layout BorderPane HBox Priority Region StackPane)
           (javafx.scene.text Font)
           (javafx.stage FileChooser FileChooser$ExtensionFilter Stage)
           (uk.org.il2ssd.jfx ConsolePresenter ConsoleView CyclePresenter
                              CycleView MainPresenter MainView
                              SettingsPresenter SettingsView
                              SinglePresenter SingleView CycleMission)
           (javafx.util Callback)))

(def modes
  "### modes
   This is a map of the mission loading modes."
  {:single "Single Mission", :cycle "Mission Cycle"})

(defn map-control-instances
  "### map-control-instances
   This function takes the main controls map as an argument and assocs the
   top-level keys to the object instances held in the main map, so that the
   instances can be accessed by key directly."
  [controls]
  (apply hash-map (interleave
                    (keys controls)
                    (map :instance (vals controls)))))

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

   The main control map contains the object instance, the global states that are
   required to enable this control, and those which disabled it. At present all
   states are required for enablement: there is no way to define an OR condition.
   For disablement, the presence of the given disabling states will disable the
   control: there is no way to define an AND condition.
   This functionality is sufficient for now.

   The main control map is then supplemented with another atom in which the
   top-level keys are mapped directly to the object instances.

   By doing this, we can refer to an object instance in any namespace by using the
   following syntax:

       (let [{:keys [<objects we want to use>]}
             @state/control-instances]
         (<function body>))

   It may be necessary to use type hinting to limit uses of the Reflection API at
   runtime for performance reasons; Clojure can't simply infer what we're going
   to hold in an atom at runtime."
  []
  (let [{:keys [^MainPresenter main-presenter
                ^ConsolePresenter console-presenter
                ^SinglePresenter single-presenter
                ^CyclePresenter cycle-presenter
                ^SettingsPresenter settings-presenter]} @state/presenters
        controls
        {;Main FXML file controls
          :tool-bar          {:instance (.getToolBar main-presenter)}
          :connect-btn       {:instance    (.getConnectButton main-presenter)
                              :disabled-by #{:connected}}
          :disconn-btn       {:instance   (.getDisconnectButton main-presenter)
                              :enabled-by #{:connected}}
          :prog-stack        {:instance (.getProgressStack main-presenter)}
          :prog-ind          {:instance (.getProgressIndicator main-presenter)}
          :start-btn         {:instance   (.getStartStopButton main-presenter)
                              :enabled-by #{:connected :single-mission-path :loaded}}
          :cycle-start-btn   {:instance   (.getCycleStartStopButton main-presenter)
                              :enabled-by #{:connected :cycle-mission-path}}
          :next-btn          {:instance   (.getNextButton main-presenter)
                              :enabled-by #{:connected :cycle-mission-path :cycle-running :playing}}
          :console-tab       {:instance (.getConsoleTab main-presenter)}
          :settings-tab      {:instance (.getSettingsTab main-presenter)}
          :mission-pane      {:instance (.getMissionPane main-presenter)}
          :mode-choice       {:instance (.getMissionModeChoice main-presenter)}
          :mission-spring    {:instance (.getMissionBarSpring main-presenter)}
          :load-btn          {:instance   (.getMissionLoadButton main-presenter)
                              :enabled-by #{:connected :single-mission-path}}
          :exit-btn          {:instance (.getExitItem main-presenter)}
          :about-btn         {:instance (.getAboutItem main-presenter)}
          :mis-chooser       {:instance (FileChooser.)}
          :dcg-chooser       {:instance (FileChooser.)}
         ;Console Tab FXML file controls
          :console-pane      {:instance (.getConsolePane console-presenter)}
          :cmd-entry         {:instance   (.getCommandEntryField console-presenter)
                              :enabled-by #{:connected}}
          :console           {:instance (.getConsoleTextArea console-presenter)}
         ;Single Mission FXML file controls
          :single-mis-pane   {:instance (.getSingleMisPane single-presenter)}
          :single-path-btn   {:instance   (.getChooseSingleMisButton single-presenter)
                              :enabled-by #{:server-path}}
          :single-path-fld   {:instance (.getSingleMisPathField single-presenter)}
          :single-remote-btn {:instance (.getRemoteSelectButton single-presenter)}
          :single-path-lbl   {:instance (.getSingleMisPathLabel single-presenter)}
         ;Mission Cycle FXML file controls
          :cycle-mis-pane    {:instance (.getCycleMisPane cycle-presenter)}
          :cycle-table       {:instance (.getCycleMissionTable cycle-presenter)}
          :cycle-data        {:instance (FXCollections/synchronizedObservableList (FXCollections/observableArrayList))}
          :cycle-mis-col     {:instance (.getCycleMissionColumn cycle-presenter)}
          :cycle-tim-col     {:instance (.getCycleTimerColumn cycle-presenter)}
          :cycle-mis-upbtn   {:instance    (.getMissionUpButton cycle-presenter)
                              :enabled-by  #{:cycle-mission-path}
                              :disabled-by #{:cycle-running}}
          :cycle-mis-delbtn  {:instance    (.getMissionDeleteButton cycle-presenter)
                              :enabled-by  #{:cycle-mission-path}
                              :disabled-by #{:cycle-running}}
          :cycle-mis-dwnbtn  {:instance    (.getMissionDownButton cycle-presenter)
                              :enabled-by  #{:cycle-mission-path}
                              :disabled-by #{:cycle-running}}
          :cycle-path-fld    {:instance (.getCycleMisPathField cycle-presenter)}
          :cycle-path-btn    {:instance   (.getChooseCycleMisButton cycle-presenter)
                              :enabled-by #{:server-path}
                              :disabled-by #{:cycle-running}}
          :cycle-mis-addbtn  {:instance (.getAddMissionButton cycle-presenter)
                              :disabled-by #{:cycle-running}}
         ;Settings Tab FXML file controls
          :settings-pane     {:instance (.getSettingsPane settings-presenter)}
          :ip-field          {:instance (.getIpAddressField settings-presenter)}
          :port-field        {:instance (.getPortField settings-presenter)}
          :server-path-lbl   {:instance (.getServerPathLabel settings-presenter)}
          :server-path-btn   {:instance (.getServerPathButton settings-presenter)}
          :server-chooser    {:instance (FileChooser.)}
          :get-diff-btn      {:instance   (.getGetDifficultyButton settings-presenter)
                              :enabled-by #{:connected}}
          :set-diff-btn      {:instance    (.getSetDifficultyButton settings-presenter)
                              :enabled-by  #{:connected}
                              :disabled-by #{:playing}}
          :diff-table        {:instance (.getDifficultyTable settings-presenter)}
          :diff-data         {:instance (FXCollections/synchronizedObservableList (FXCollections/observableArrayList))}
          :diff-set-col      {:instance (.getDiffSettingColumn settings-presenter)}
          :diff-val-col      {:instance (.getDiffValueColumn settings-presenter)}}
        control-instances (map-control-instances controls)]
    (reset! state/controls controls)
    (reset! state/control-instances control-instances)))

(defn init-handlers
  "### init-handlers
   This zero argument function is used to add event handlers and change listeners
   to any objects that must respond to user input.
   We also add watch functions to the global state atoms where those state atoms'
   values should be reflected in the UI.
   Changes in these atoms' values will trigger a watch function that sets any
   event-specific state, and sets the control enabled states globally."
  []
  (let [{:keys [^Button connect-btn
                ^Button disconn-btn
                ^Button start-btn
                ^Button cycle-start-btn
                ^Button next-btn
                ^TextField cmd-entry
                ^ChoiceBox mode-choice
                ^Button load-btn
                ^MenuItem exit-btn
                ^Label server-path-lbl
                ^Button server-path-btn
                ^Button get-diff-btn
                ^Button set-diff-btn
                ^Button single-remote-btn
                ^Button single-path-btn
                ^Label single-path-lbl
                ^Button cycle-mis-upbtn
                ^Button cycle-mis-delbtn
                ^Button cycle-mis-dwnbtn
                ^Button cycle-mis-addbtn
                ^Button cycle-path-btn]}
        @state/control-instances
        watch-fn (partial main/update-ui state/get-state @state/controls)]
    ;State atom watch functions
    (add-watch state/connected :connected watch-fn)
    (add-watch state/loaded :loaded watch-fn)
    (add-watch state/playing :playing watch-fn)
    (add-watch state/server-path :server-path watch-fn)
    (add-watch state/single-mission-path :single-mission-path watch-fn)
    (add-watch state/cycle-mission-path :cycle-mission-path watch-fn)
    (add-watch state/cycle-running :cycle-running watch-fn)
    ;Main UI EventHandlers and Listeners
    (util/button-handler connect-btn main/connect-command)
    (util/button-handler disconn-btn main/disconnect-command)
    (util/button-handler start-btn main/start-stop-command)
    (util/button-handler cycle-start-btn main/start-stop-cycle-command)
    (util/button-handler next-btn main/next-command)
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
   The zero argument function is used to load any subsidiary FXML files.

   It is also is used to initialise any controls with default values.

   If a configuration file is found, the UI state is restored using the
   values retrieved from this file.

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
                ^Region mission-spring
                ^List cycle-data]} @state/control-instances
        config (-> (read-config-file) (get-configuration))
        {ip         :ip-field
         port       :port-field
         srv-path   :server-path-lbl
         mode-key   :mode-choice
         single-mis :single-path-lbl
         cycle      :cycle-data
         :or        {:ip-field        ""
                     :port-field      ""
                     :server-path-lbl "..."
                     :mode-choice     "single"
                     :single-path-lbl "..."}} config
        mode (-> mode-key keyword modes)]
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
          (.setText single-path-lbl single-mis)
          (when (seq cycle)
            (loop [index 0]
              (when-let [saved-mission (get cycle (str index))]
                (let [[mission timer] (edn/read-string saved-mission)]
                  (.add cycle-data (CycleMission. mission timer))
                  (reset! state/cycle-mission-path mission))
                (recur (inc index))))))
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
                ^FileChooser dcg-chooser]} @state/control-instances]
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
   ComboBoxTableCell and define the acceptable values, which produces
   table cells containing a combo box from which these values can be
   chosen.

   The backing list for the table is instantiated and stored in the controls
   atom before being linked to the table.

   Finally, we attach an EventHandler to the cell edit commit action which rejects
   any inputs which are not equal to 0 or 1, as these are the permitted values for
   Il-2 difficulty settings."
  []
  (let [{:keys [^TableView diff-table
                ^List diff-data
                ^TableColumn diff-set-col
                ^TableColumn diff-val-col]} @state/control-instances]
    (.setCellValueFactory diff-set-col (PropertyValueFactory. "setting"))
    (doto diff-val-col
      (.setCellFactory
        ^Callback (ComboBoxTableCell/forTableColumn
                    ^List (FXCollections/observableArrayList (into-array String ["0" "1"]))))
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
                ^TableColumn cycle-tim-col]} @state/control-instances]
    (.setCellValueFactory cycle-mis-col (PropertyValueFactory. "mission"))
    (doto cycle-tim-col
      (.setCellFactory (TextFieldTableCell/forTableColumn))
      (.setCellValueFactory (PropertyValueFactory. "timer"))
      (.setOnEditCommit (ui/cycle-timer-commit)))
    (doto cycle-table
      (.setColumnResizePolicy TableView/CONSTRAINED_RESIZE_POLICY)
      (.setItems cycle-data))))
