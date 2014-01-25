;; ## UI input event handling and channel listeners
;;
;; These functions define listeners and event handlers that will be applied to
;; the UI controls used in the program and update the state of these controls.
;;
;; We also define channel listeners which parse the server console output and
;; update the UI accordingly.
(ns uk.org.il2ssd.event

  (:require [clojure.core.async :refer [go thread <!!]]
            [clojure.set :refer [map-invert]]
            [clojure.string :as string]
            [uk.org.il2ssd.channel :refer :all]
            [uk.org.il2ssd.parse :as parse]
            [uk.org.il2ssd.server :as server]
            [uk.org.il2ssd.settings :as settings]
            [uk.org.il2ssd.state :as state]
            [uk.org.il2ssd.jfx.ui :as ui])

  (:import (javafx.application Platform)
           (javafx.stage Stage FileChooser)
           (uk.org.il2ssd DifficultySetting CycleMission)
           (javafx.scene.control TextArea Button TextField TableView Label ChoiceBox TableColumn$CellEditEvent
                                 TableColumn TablePosition)
           (javafx.scene.input KeyEvent)
           (javafx.scene.layout BorderPane)
           (javafx.beans InvalidationListener)
           (javafx.beans.value ChangeListener)
           (javafx.event EventHandler)
           (java.io File)
           (javafx.scene.control.cell)
           (java.util List)
           (java.nio.file Paths Path)))

(defn get-resolved-path
  [root-path in-path]
  (let [path (Paths/get in-path (into-array [""]))
        server-path (Paths/get root-path (into-array [""]))
        server-dir (.getParent server-path)
        mis-dir (.resolve server-dir "Missions")]
    (string/replace (->> path (.relativize mis-dir) .normalize str) "\\" "/")))


(defn mis-selected?
  []
  (let [{:keys [single-path-lbl]} @state/controls
        single-mis (ui/get-text single-path-lbl)]
    (when (= @state/mode "single")
      (if (not (string/blank? single-mis))
        true
        false))
    (when (= @state/mode "cycle")
      false)
    (when (= @state/mode "dcg")
      false)))

(defn start-stop-command
  "### start-stop-command
   This is a zero argument function which ends the current mission if is playing,
   or starts the currently loaded mission if it is not."
  []
  (if @state/playing
    (server/end-mission)
    (server/start-mission)))

(defn load-unload-command
  "### load-unload-command
   This is a zero argument function which unloads the currently loaded mission if
   it is loaded."
  []
  (if @state/loaded
    (server/unload-mission)
    (when (= @state/mode "single")
      (server/load-mission @state/mission-path))))

(defn get-difficulties
  "### get-difficulties
   This is a zero argument function which clears the difficulty data list and
   requests the currently loaded difficulty settings from the server so that they
   can be parsed back into the program by the listener function."
  []
  (let [{:keys [diff-data]} @state/controls]
    (ui/clear-diff-data diff-data)
    (server/get-difficulty)))

(defn set-difficulties
  "### set-difficulties
   This is a zero argument function which iterates over the difficulty data list
   and gets the setting and value for each DifficultySetting instance in the list,
   setting each provided setting to the provided value on the server.

   This will update the server with any changes that the user has made to the
   difficulty settings list"
  []
  (let [{:keys [diff-data]} @state/controls]
    (doseq [^DifficultySetting item diff-data]
      (let [setting (.getSetting item)
            value (.getValue item)]
        (server/set-difficulty setting value)))))

(defn console-listener
  "### console-listener
   This is a zero argument function which spawns another thread. The process on
   this thread listens for non-nil output on the print-channel for as long as the
   global connection state atom says that we are connected. Any text which is read
   is printed to the application console.

   Functions which spawn a thread return immediately so that execution can
   proceed in the other thread. This prevents these functions from blocking
   the current thread."
  []
  (thread (while @state/connected
            (let [text (<!! print-channel)]
              (when text
                (let [{:keys [console]} @state/controls]
                  (ui/print-console console text)))))))


(defn difficulty-listener
  "### difficulty-listener
   This is a zero argument function which spawns another thread. The process on
   this thread listens for non-nil output on the diff-channel for as long as the
   global connection state atom says that we are connected.

   Any text which is read is parsed into a setting-value pair which is used to
   instantiate a DifficultySetting object in the difficulty settings list.

   As above, we should note that this function will return immediately and
   execution will continue on another thread without blocking this one."
  []
  (thread (while @state/connected
            (let [text (<!! diff-channel)]
              (when text
                (let [parsed (parse/difficulty-parser text)
                      {:keys [diff-data]} @state/controls
                      setting ((parsed 1) 1)
                      value ((parsed 2) 1)]
                  (ui/add-diff-data diff-data (DifficultySetting. setting value))))))))

(defn set-title
  "### set-title
   This is a multiple-arity function which changes the title of the program
   window.

   When the function is called with no arguments it resets the title to the
   default.

   When it is called with two arguments it adds the currently loaded mission
   and its current state to the title."
  ([]
   (ui/set-title @state/stage "Il-2 Simple Server Daemon"))
  ([mission state]
   (ui/set-title @state/stage
                 (str "Il-2 Simple Server Daemon - " mission " " (string/lower-case state)))))

(defn mission-listener
  "### mission-listener
   This is a zero argument function which spawns another thread. The process on
   this thread listens for non-nil output on the mis-channel for as long as the
   global connection state atom says that we are connected.

   Any text which is taken from the channel is parsed by the mission parser.

   If the parser returns one element we know that a mission and its path were not
   found. This can only mean that no mission was loaded. In this case we set the
   global state atoms to false and reset the title to the default.

   If the parser returns three elements we know that a mission is loaded and that
   we can use the state returned for further processing. We reset the global
   state atoms depending upon the state returned and add the mission and state
   to the stage title.

   Any text which doesn't match the parser rules does not trigger any further
   processing.

   Functions which spawn a thread return immediately so that execution can
   proceed in the other thread. This prevents these functions from blocking
   the current thread."
  []
  (thread (while @state/connected
            (let [text (<!! mis-channel)]
              (when text
                (let [parsed (parse/mission-parser text)]
                  (when (nil? (get parsed 2))
                    (let [state (get-in parsed [1 1])]
                      (when (= state "NOT loaded")
                        (reset! state/loaded false)
                        (reset! state/playing false)
                        (set-title))))
                  (when (seq (get parsed 3))
                    (let [path (get-in parsed [1 1])
                          mission (get-in parsed [2 1])
                          state (get-in parsed [3 1])]
                      (when (= state "Loaded")
                        (reset! state/loaded true)
                        (reset! state/playing false))
                      (when (= state "Playing")
                        (reset! state/loaded true)
                        (reset! state/playing true))
                      (set-title mission state)))))))))

(defn err-listener
  "### err-listener
   This is a zero argument function which listens for text on the error channel.
   This channel filters for mission loading errors, so when we receive a value
   from this channel we know that the requested mission failed to load, and we
   can set the mission state to unloaded."
  []
  (thread (while @state/connected
            (let [text (<!! err-channel)]
              (when text
                (reset! state/loaded false)
                (reset! state/playing false)
                (set-title))))))

(defn start-listeners
  "### start-listeners
   This is a zero argument convenience function which starts all of the listeners
   which parse the server console output. They all need to be running and removing
   puts from the channels or the program will stall."
  []
  (console-listener)
  (difficulty-listener)
  (mission-listener)
  (err-listener))

(defn set-connected
  "### set-connected
   This is a watch function which sets the UI controls to the correct state
   for the connection status defined by the connected argument.

   It also starts all of the listeners which parse output from the server console.

   Because these listeners spawn on a separate thread, their functions return
   immediately and the current thread does not block."
  [_ _ _ connected]
  (let [{:keys [diff-data]} @state/controls]
    (ui/set-ui-connected connected @state/controls)
    (if connected
      (start-listeners)
      (do (set-title)
          (ui/clear-diff-data diff-data)))))

(defn set-mission-playing
  "### set-mission-playing
   This is a watch function which sets the UI controls to the correct state for
   the mission playing status defined by the playing argument."
  [_ _ _ playing]
  (ui/set-ui-playing playing @state/controls))

(defn set-mission-loaded
  "### set-mission-loaded
   This is a watch function which sets the UI controls to the correct state for
   the mission loaded status defined by the loaded argument."
  [_ _ _ loaded]
  (ui/set-ui-loaded loaded @state/mission-path @state/controls))


(defn enter-command
  "### enter-command
   This is zero argument function which checks whether the text in the TextField
   control is \"clear\". In that case we clear the application's server console
   text. For all other text values, we send the entered text as a command to the
   server."
  []
  (let [{:keys [cmd-entry
                console]} @state/controls]
    (if (= (ui/get-text cmd-entry) "clear")
      (do (ui/clear-input console)
          (ui/clear-input cmd-entry))
      (do (server/write-socket (ui/get-text cmd-entry))
          (ui/clear-input cmd-entry)))))

(defn connect-command
  "### connect-command
   This is a zero argument function which calls the server connect function with
   the host from the IP field and the port from the port field.

   We wrap this connection attempt in a go block so that it returns immediately,
   preventing the current thread from blocking.

   We also wrap the connection attempt in a try/catch block to catch any
   exceptions which arise from converting the port-field text to an integer."
  []
  (let [{:keys [ip-field
                port-field]} @state/controls
        ip (ui/get-text ip-field)
        port (ui/get-text port-field)]
    (go (try (server/connect ip (Integer/decode port))
             (catch NumberFormatException e nil)
             (catch NullPointerException e nil)))))

(defn disconnect-command
  "### disconnect-command
   This is a zero argument function which calls the server disconnect function
   in a go block so that it doesn't block the calling thread."
  []
  (go (server/disconnect)))

(defn mode-choice
  "### mode-choice
   This is a one argument function which uses the content of the modes argument to
   retrieve the key of the mode text selected.

   This key is used to load the correct UI pane for the mission mode the user has
   selected.

   After the UI pane has been loaded the field contents specific to this pane are
   saved and finally the mode setting is saved."
  [modes]
  (let [{:keys [mission-pane
                single-mis-pane
                cycle-mis-pane
                mode-choice]} @state/controls
        mode (name ((map-invert modes) (ui/get-choice mode-choice)))]
    (reset! state/mode mode)
    (when (= mode "single")
      (ui/set-mis-pane mission-pane single-mis-pane))
    (when (= mode "cycle")
      (ui/set-mis-pane mission-pane cycle-mis-pane))))

(defn server-path-select
  []
  (let [{:keys [server-path-lbl]} @state/controls
        server-path (ui/get-text server-path-lbl)]
    (if (= server-path "...")
      (reset! state/server-path nil)
      (reset! state/server-path server-path))))

(defn server-choose-command
  "### server-choose-command
   This zero argument function displays the server chooser dialog and uses
   the provided file to set the server path in the UI."
  []
  (let [{:keys [server-chooser
                server-path-lbl]} @state/controls
        file (ui/show-chooser server-chooser)]
    (when file
      (ui/set-label server-path-lbl (.getCanonicalPath file)))))

(defn set-single-remote
  []
  (let [{:keys [single-path-fld single-path-lbl]} @state/controls
        mission-path (ui/get-text single-path-fld)]
    (when (not (string/blank? mission-path))
      (ui/set-label single-path-lbl mission-path))))

(defn single-choose-command
  []
  (let [{:keys [mis-chooser
                single-path-lbl]} @state/controls
        file (ui/show-chooser mis-chooser)]
    (when file
      (ui/set-label single-path-lbl
                    (get-resolved-path @state/server-path (.getCanonicalPath file))))))

(defn single-path-select
  []
  (let [{:keys [single-path-lbl]} @state/controls
        single-path (ui/get-text single-path-lbl)]
    (if (= single-path "...")
      (reset! state/mission-path nil)
      (reset! state/mission-path single-path))))

(defn set-server-selected
  [_ _ _ path]
  (ui/set-ui-server path @state/controls)
  (ui/set-mis-dir path @state/controls))

(defn set-mis-selected
  [_ _ _ selected]
  (ui/set-ui-mis selected @state/connected @state/loaded @state/controls))

(defn save-ui-state
  []
  (let [{:keys [mode-choice
                ip-field
                port-field
                server-path-lbl
                single-path-lbl
                cycle-data]} @state/controls
        mode @state/mode
        ip-addr (ui/get-text ip-field)
        port (ui/get-text port-field)
        server-path (ui/get-text server-path-lbl)
        single-path (ui/get-text single-path-lbl)]
    (settings/save-server ip-addr port server-path)
    (settings/save-mission mode single-path)))

(defn close
  "### close
   This is a zero argument function which disconnects from the server if the
   program is connected, saves the current settings to the config file and
   requests to close the program."
  []
  (do (if @state/connected (server/disconnect))
      (save-ui-state)
      (settings/save-config-file)
      (ui/exit)))