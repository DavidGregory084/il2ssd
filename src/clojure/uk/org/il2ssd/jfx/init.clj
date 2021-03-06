;;
;; ## UI initialisation functions
;;
;; In this namespace we define the functions which instantiate any objects that
;; we need and initialise the objects we retrieve using afterburner.fx into data
;; structures which we can more easily manipulate in Clojure.
(ns uk.org.il2ssd.jfx.init
  (:require [clojure.java.io :refer [resource]]
            [clojure.edn :as edn]
            [uk.org.il2ssd.event.bans :as bans]
            [uk.org.il2ssd.event.console :as console]
            [uk.org.il2ssd.event.cycle :as cycle]
            [uk.org.il2ssd.event.dcg :as dcg]
            [uk.org.il2ssd.event.main :as main]
            [uk.org.il2ssd.event.mission :as mission]
            [uk.org.il2ssd.event.pilots :as pilots]
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
                                 TableColumn TableView Tab TextField ToggleButton)
           (javafx.scene.control.cell PropertyValueFactory
                                      TextFieldTableCell ChoiceBoxTableCell
                                      ComboBoxTableCell)
           (javafx.scene.layout BorderPane HBox Priority Region StackPane)
           (javafx.scene.text Font)
           (javafx.stage FileChooser FileChooser$ExtensionFilter Stage)
           (uk.org.il2ssd.jfx ConsolePresenter ConsoleView CyclePresenter
                              CycleView MainPresenter MainView
                              SettingsPresenter SettingsView
                              SinglePresenter SingleView CycleMission DCGView
                              DCGPresenter PilotsView PilotsPresenter BansView
                              BansPresenter)
           (javafx.util Callback)))

(def modes
  "### modes
   This is a map of the mission loading modes."
  {:single "Single Mission", :cycle "Mission Cycle", :dcg "DCG Generation"})

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
        _ (Font/loadFont
            (.toExternalForm ^URL (resource "fontawesome-webfont.ttf")) 12.0)
        main-view (MainView.)
        console-view (ConsoleView.)
        pilots-view (PilotsView.)
        bans-view (BansView.)
        single-view (SingleView.)
        cycle-view (CycleView.)
        dcg-view (DCGView.)
        settings-view (SettingsView.)
        scene (Scene. (.getView main-view))
        main-presenter (.getPresenter main-view)
        console-presenter (.getPresenter console-view)
        pilots-presenter (.getPresenter pilots-view)
        bans-presenter (.getPresenter bans-view)
        single-presenter (.getPresenter single-view)
        cycle-presenter (.getPresenter cycle-view)
        dcg-presenter (.getPresenter dcg-view)
        settings-presenter (.getPresenter settings-view)]
    (main/set-title)
    (doto stage
      (.setScene scene)
      (.setResizable false)
      (.show))
    (util/close-handler stage main/close)
    (reset! state/presenters
            {:main-presenter     main-presenter
             :console-presenter  console-presenter
             :pilots-presenter   pilots-presenter
             :bans-presenter     bans-presenter
             :single-presenter   single-presenter
             :cycle-presenter    cycle-presenter
             :dcg-presenter      dcg-presenter
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
                ^PilotsPresenter pilots-presenter
                ^BansPresenter bans-presenter
                ^SinglePresenter single-presenter
                ^CyclePresenter cycle-presenter
                ^DCGPresenter dcg-presenter
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
                              :enabled-by #{:connected :loaded}}
          :cycle-start-btn   {:instance   (.getCycleStartStopButton main-presenter)
                              :enabled-by #{:connected :cycle-mission-path}
                              :disabled-by #{:loading}}
          :dcg-start-btn     {:instance   (.getDcgStartStopButton main-presenter)
                              :enabled-by #{:connected :dcg-path}
                              :disabled-by #{:loading}}
          :cycle-next-btn    {:instance   (.getCycleNextButton main-presenter)
                              :enabled-by #{:connected :cycle-mission-path :cycle-running :playing}
                              :disabled-by #{:loading}}
          :dcg-next-btn      {:instance   (.getDcgNextButton main-presenter)
                              :enabled-by #{:connected :dcg-running :playing}
                              :disabled-by #{:loading}}
          :console-tab       {:instance (.getConsoleTab main-presenter)}
          :pilots-tab        {:instance (.getPilotsTab main-presenter)}
          :bans-tab          {:instance (.getBansTab main-presenter)}
          :settings-tab      {:instance (.getSettingsTab main-presenter)}
          :mission-pane      {:instance (.getMissionPane main-presenter)}
          :mode-choice       {:instance (.getMissionModeChoice main-presenter)}
          :mission-spring    {:instance (.getMissionBarSpring main-presenter)}
          :load-btn          {:instance   (.getMissionLoadButton main-presenter)
                              :enabled-by #{:connected :single-mission-path}
                              :disabled-by #{:loading}}
          :exit-btn          {:instance (.getExitItem main-presenter)}
          :about-btn         {:instance (.getAboutItem main-presenter)}
          :mis-chooser       {:instance (FileChooser.)}
          :dcg-chooser       {:instance (FileChooser.)}
         ;Console Tab FXML file controls
          :console-pane      {:instance (.getConsolePane console-presenter)}
          :cmd-entry         {:instance   (.getCommandEntryField console-presenter)
                              :enabled-by #{:connected}}
          :console           {:instance (.getConsoleTextArea console-presenter)}
         ;Pilots Tab FXML file controls
          :pilots-pane       {:instance (.getPilotsPane pilots-presenter)}
          :pilots-table      {:instance (.getPilotsTable pilots-presenter)}
          :pilots-data       {:instance (FXCollections/synchronizedObservableList (FXCollections/observableArrayList))}
          :pilot-number-col  {:instance (.getPilotNumberColumn pilots-presenter)}
          :pilot-socket-col  {:instance (.getPilotSocketColumn pilots-presenter)}
          :pilot-ip-col      {:instance (.getPilotIpColumn pilots-presenter)}
          :pilot-name-col    {:instance (.getPilotNameColumn pilots-presenter)}
          :pilot-score-col   {:instance (.getPilotScoreColumn pilots-presenter)}
          :pilot-team-col    {:instance (.getPilotTeamColumn pilots-presenter)}
          :pilot-upd-fld     {:instance (.getPilotUpdateTimerField pilots-presenter)}
          :kick-btn          {:instance (.getKickButton pilots-presenter)
                              :enabled-by #{:connected}}
          :ban-btn           {:instance (.getBanButton pilots-presenter)
                              :enabled-by #{:connected}}
          :ip-ban-btn        {:instance (.getIpBanButton pilots-presenter)
                              :enabled-by #{:connected}}
          :chat-field        {:instance (.getChatField pilots-presenter)
                              :enabled-by #{:connected}}
          :send-chat-btn     {:instance (.getSendChatButton pilots-presenter)
                              :enabled-by #{:connected}}
         ;Ban List FXML file controls
          :bans-pane         {:instance (.getBansPane bans-presenter)}
          :bans-table        {:instance (.getBansTable bans-presenter)}
          :bans-data         {:instance (FXCollections/synchronizedObservableList (FXCollections/observableArrayList))}
          :ban-type-col      {:instance (.getBanTypeColumn bans-presenter)}
          :ban-value-col     {:instance (.getBanValueColumn bans-presenter)}
          :get-bans-btn      {:instance (.getGetBansButton bans-presenter)
                              :enabled-by #{:connected}}
          :lift-ban-btn    {:instance (.getRemoveBanButton bans-presenter)
                              :enabled-by #{:connected}}
          :clear-bans-btn    {:instance (.getClearBansButton bans-presenter)
                              :enabled-by #{:connected}}
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
                              :enabled-by #{:server-path}}
          :cycle-mis-addbtn  {:instance (.getAddMissionButton cycle-presenter)}
         ;DCG Mode FXML file controls
          :dcg-mis-pane      {:instance (.getDcgMisPane dcg-presenter)}
          :dcg-timer-toggle  {:instance (.getDcgMisTimerToggle dcg-presenter)}
          :dcg-timer-fld     {:instance (.getDcgMisTimerField dcg-presenter)}
          :dcg-mis-lbl       {:instance (.getDcgMisPathLabel dcg-presenter)}
          :dcg-path-lbl      {:instance (.getDcgExePathLabel dcg-presenter)}
          :dcg-path-btn      {:instance (.getDcgExePathSelectButton dcg-presenter)}
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
                              :disabled-by #{:loading :playing}}
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
                ^Button cycle-next-btn
                ^TextField cmd-entry
                ^Button kick-btn
                ^Button ban-btn
                ^Button ip-ban-btn
                ^Button send-chat-btn
                ^TextField chat-field
                ^Button get-bans-btn
                ^Button lift-ban-btn
                ^Button clear-bans-btn
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
                ^Button cycle-path-btn
                ^ToggleButton dcg-timer-toggle
                ^Button dcg-path-btn
                ^Button dcg-start-btn
                ^Button dcg-next-btn
                ^Label dcg-path-lbl
                ^Label dcg-mis-lbl]}
        @state/control-instances
        watch-fn (partial main/update-ui state/get-state @state/controls)]
    ;State atom watch functions
    (add-watch state/connected :connected watch-fn)
    (add-watch state/loaded :loaded watch-fn)
    (add-watch state/loading :loading watch-fn)
    (add-watch state/playing :playing watch-fn)
    (add-watch state/server-path :server-path watch-fn)
    (add-watch state/dcg-path :dcg-path watch-fn)
    (add-watch state/single-mission-path :single-mission-path watch-fn)
    (add-watch state/cycle-mission-path :cycle-mission-path watch-fn)
    (add-watch state/dcg-timer :dcg-timer watch-fn)
    (add-watch state/dcg-mission-path :dcg-mission-path watch-fn)
    (add-watch state/cycle-running :cycle-running watch-fn)
    (add-watch state/cycle-index :cycle-index watch-fn)
    (add-watch state/dcg-running :dcg-running watch-fn)
    ;Main UI EventHandlers and Listeners
    (util/button-handler connect-btn main/connect-command)
    (util/button-handler disconn-btn main/disconnect-command)
    (util/button-handler start-btn main/start-stop-command)
    (util/button-handler load-btn main/load-unload-command)
    (util/button-handler exit-btn main/close)
    ;Console tab
    (util/keypress-handler cmd-entry "Enter" console/enter-command)
    ;Pilots tab
    (util/button-handler kick-btn pilots/kick-pilot)
    (util/button-handler ban-btn pilots/ban-pilot)
    (util/button-handler ip-ban-btn pilots/ip-ban-pilot)
    (util/button-handler send-chat-btn pilots/send-chat)
    (util/keypress-handler chat-field "Enter" pilots/send-chat)
    ;Ban List tab
    (util/button-handler get-bans-btn bans/get-bans)
    (util/button-handler lift-ban-btn bans/lift-ban)
    (util/button-handler clear-bans-btn bans/clear-bans)
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
    (util/button-handler cycle-start-btn cycle/start-stop-cycle-command)
    (util/button-handler cycle-next-btn cycle/next-mission false)
    ;DCG generation pane
    (util/button-handler dcg-timer-toggle dcg/toggle-timer)
    (util/button-handler dcg-path-btn dcg/dcg-choose-command)
    (util/button-handler dcg-start-btn dcg/start-stop-dcg-command)
    (util/button-handler dcg-next-btn dcg/generate-dcg-mis false)
    (util/text-listener dcg-path-lbl dcg/dcg-path-select)
    (util/text-listener dcg-mis-lbl dcg/dcg-mis-generated)
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
                ^Tab pilots-tab
                ^Tab bans-tab
                ^Tab settings-tab
                ^BorderPane console-pane
                ^BorderPane pilots-pane
                ^BorderPane bans-pane
                ^BorderPane settings-pane
                ^TextField ip-field
                ^TextField port-field
                ^TextField pilot-upd-fld
                ^Label server-path-lbl
                ^Label single-path-lbl
                ^ToggleButton dcg-timer-toggle
                ^TextField dcg-timer-fld
                ^Label dcg-path-lbl
                ^StackPane prog-stack
                ^ChoiceBox mode-choice
                ^Region mission-spring
                ^List cycle-data]} @state/control-instances
        config (get-configuration (read-config-file))
        {ip         :ip-field
         port       :port-field
         srv-path   :server-path-lbl
         mode-key   :mode-choice
         single-mis :single-path-lbl
         cycle      :cycle-data
         dcg-timer  :dcg-timer-toggle
         dcg-mins   :dcg-timer-fld
         dcg-path   :dcg-path-lbl
         pilot-upd  :pilot-upd-fld
         :or        {:ip-field        ""
                     :port-field      ""
                     :server-path-lbl "..."
                     :mode-choice     "single"
                     :single-path-lbl "..."
                     :dcg-timer-toggle "false"
                     :dcg-timer-fld ""
                     :dcg-path-lbl "..."
                     :pilot-upd-fld "10"}} config
        mode (-> mode-key keyword modes)]
    (HBox/setHgrow prog-stack Priority/ALWAYS)
    (HBox/setHgrow mission-spring Priority/ALWAYS)
    (.setContent console-tab console-pane)
    (.setContent pilots-tab pilots-pane)
    (.setContent bans-tab bans-pane)
    (.setContent settings-tab settings-pane)
    (-> mode-choice .getItems (.addAll ^List (map modes [:single :cycle :dcg])))
    (if config
      (do (-> mode-choice .getSelectionModel (.select mode))
          (.setText ip-field ip)
          (.setText port-field port)
          (.setText server-path-lbl srv-path)
          (.setText single-path-lbl single-mis)
          (.setText dcg-path-lbl dcg-path)
          (.setText pilot-upd-fld pilot-upd)
          (case dcg-timer
            "true" (.setSelected dcg-timer-toggle true)
            "false" (.setSelected dcg-timer-toggle false))
          (dcg/toggle-timer)
          (.setText dcg-timer-fld dcg-mins)
          (when (seq cycle)
            (loop [index 0]
              (when-let [saved-mission (get cycle (str index))]
                (let [[mission timer] (edn/read-string saved-mission)]
                  (.add cycle-data (CycleMission. mission timer))
                  (when (zero? index)
                    (reset! state/cycle-mission-path mission)))
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

(defn init-pilots-table
  []
  (let [{:keys [^TableView pilots-table
                ^List pilots-data
                ^TableColumn pilot-number-col
                ^TableColumn pilot-socket-col
                ^TableColumn pilot-ip-col
                ^TableColumn pilot-name-col
                ^TableColumn pilot-score-col
                ^TableColumn pilot-team-col]} @state/control-instances]
    (.setCellValueFactory pilot-number-col (PropertyValueFactory. "number"))
    (.setCellValueFactory pilot-socket-col (PropertyValueFactory. "socket"))
    (.setCellValueFactory pilot-ip-col (PropertyValueFactory. "ip"))
    (.setCellValueFactory pilot-name-col (PropertyValueFactory. "name"))
    (.setCellValueFactory pilot-score-col (PropertyValueFactory. "score"))
    (.setCellValueFactory pilot-team-col (PropertyValueFactory. "team"))
    (doto pilots-table
      (.setColumnResizePolicy TableView/CONSTRAINED_RESIZE_POLICY)
      (.setItems pilots-data)
      (-> .getSortOrder
          (.addAll
            (into-array TableColumn [pilot-team-col
                                     pilot-score-col
                                     pilot-number-col]))))))

(defn init-bans-table
  []
  (let [{:keys [^TableView bans-table
                ^List bans-data
                ^TableColumn ban-type-col
                ^TableColumn ban-value-col]} @state/control-instances]
    (.setCellValueFactory ban-type-col (PropertyValueFactory. "type"))
    (.setCellValueFactory ban-value-col (PropertyValueFactory. "value"))
    (doto bans-table
      (.setColumnResizePolicy TableView/CONSTRAINED_RESIZE_POLICY)
      (.setItems bans-data))))