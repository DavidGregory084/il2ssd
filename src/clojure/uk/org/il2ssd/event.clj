;; ## UI input event handling and channel listeners
;;
;; These functions define listeners and event handlers that will be applied to
;; the UI controls used in the program and update the state of these controls.
;;
;; We also define channel listeners which parse the server console output and
;; update the UI accordingly.
(ns uk.org.il2ssd.event

  (:require [clojure.core.async :refer [go thread <!!]]
            [clojure.data :refer [diff]]
            [clojure.set :refer [map-invert]]
            [clojure.string :as string]
            [uk.org.il2ssd.channel :refer :all]
            [uk.org.il2ssd.parse :refer :all]
            [uk.org.il2ssd.server :as server]
            [uk.org.il2ssd.settings :as settings]
            [uk.org.il2ssd.state :as state]
            [uk.org.il2ssd.jfx.ui :as ui])

  (:import (javafx.application Platform)
           (javafx.stage Stage FileChooser)
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
           (java.nio.file LinkOption Paths Path)))

(defn get-relative-path
  "### get-relative-path
   This two-argument function returns a relative path for a mission based upon
   the two input paths.

   The server path is expected to be the string path to the il2server.exe file.

   The mission path should be the full canonical path string of the mission file
   we wish to load.

   In order to determine the path to send to the server we convert the strings
   to Path objects, get the parent path of the server .exe file (which should be
   the main server directory), and resolve this against the string \"Missions\".

   This Path object should represent the Missions directory of our server.

   Finally, we relativise the input path against the Missions directory path to
   get a relative path to the mission from the Missions directory."
  [server-path mis-path]
  (let [in-path (Paths/get mis-path (into-array String []))
        server-exe (Paths/get server-path (into-array String []))
        server-dir (.getParent server-exe)
        mis-dir (.resolve server-dir "Missions")
        out-path (->> in-path (.relativize mis-dir) str)]
    (string/replace out-path "\\" "/")))

(defn mis-selected?
  "### mis-selected?
   This zero argument function is used to determine whether there is a valid
   mission selection, dependent upon the current UI mission loading mode."
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
   it is loaded, and loads the current mission (dependent upon the mission loading
   mode) if it is not."
  []
  (if @state/loaded
    (server/unload-mission)
    (when (= @state/mode "single")
      (ui/toggle-prog-ind @state/controls true)
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
   and gets the setting and value for each difficulty setting in the list,
   setting each provided setting to the provided value on the server.

   This will update the server with any changes that the user has made to the
   difficulty settings list."
  []
  (let [{:keys [diff-data]} @state/controls]
    (doseq [item diff-data]
      (let [item-data (ui/get-item-data item)
            setting (:setting item-data)
            value (:value item-data)]
        (server/set-difficulty setting value)))))

(defn console-listener
  "### console-listener
   This is a zero argument function which spawns another thread. The process on
   this thread listens for non-nil output on the print-channel for as long as the
   global connection state atom says that we are connected. Any text which is read
   is printed to the application console.

   Functions which spawn a thread return immediately so that execution can
   proceed in the new thread. This prevents these functions from blocking
   the calling thread."
  []
  (thread (while @state/connected
            (when-let [text (<!! print-channel)]
              (let [{:keys [console]} @state/controls]
                (ui/print-console console text))))))


(defn difficulty-listener
  "### difficulty-listener
   This is a zero argument function which spawns another thread. The process on
   this thread listens for non-nil output on the diff-channel for as long as the
   global connection state atom says that we are connected.

   Any text which is read is parsed into a setting-value pair which is used to
   add a new setting-value pair to the difficulty settings list.

   As above, we should note that this function will return immediately and
   execution will continue on a new thread without blocking the caller."
  []
  (thread (while @state/connected
            (when-let [text (<!! diff-channel)]
              (let [parsed (parse-text difficulty-parser text)
                    {:keys [diff-data]} @state/controls
                    {:keys [setting value]} parsed]
                (ui/add-diff-data diff-data setting value))))))

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

   Any text which is taken from the channel is parsed into a map of key-value
   pairs.

   If we have a mission key we know that a mission is loaded. We can use this
   and the value of the mission state key to determine how to set the UI.

   Any text which doesn't match the parser rules does not trigger any further
   processing.

   Functions which spawn a thread return immediately so that execution can
   proceed in the new thread. This prevents these functions from blocking
   the calling thread."
  []
  (thread (while @state/connected
            (when-let [text (<!! mis-channel)]
              (let [parsed (parse-text mission-parser text)]
                (if (nil? (:mission parsed))
                  (let [state (:state parsed)]
                    (when (= state "NOT loaded")
                      (reset! state/loaded false)
                      (reset! state/playing false)
                      (set-title)))
                  (let [{:keys [mission state]} parsed]
                    (when (= state "Loaded")
                      (reset! state/loaded true)
                      (reset! state/playing false))
                    (when (= state "Playing")
                      (reset! state/loaded true)
                      (reset! state/playing true))
                    (set-title mission state))))))))

(defn err-listener
  "### err-listener
   This is a zero argument function which listens for text on the error channel.

   This channel filters for mission loading errors, so when we receive a value
   from this channel we know that the requested mission failed to load, and we
   can set the mission state to unloaded."
  []
  (thread (while @state/connected
            (when-let [text (<!! err-channel)]
              (ui/toggle-prog-ind @state/controls false)
              (reset! state/loaded false)
              (reset! state/playing false)
              (set-title)))))

(defn start-listeners
  "### start-listeners
   This is a zero argument convenience function which starts all of the listeners
   which parse the server console output.

   They all need to be running and removing puts from the channels or the program
   will stall - every tap must take each value from the mult to stay synchronised."
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
   control is \"clear\".

   In that case we clear the application's server console text. For all other text
   values, we send the entered text as a command to the server."
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
   selected."
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
  "### server-path-select
   This zero argument function sets the global server path atom. It is
   called when the server path label text changes."
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
  "### set-single-remote
   This zero argument function sets the UI label which defines the mission
   to load based upon the content of the remote single mission text field.

   This is used when the user does not have local access to the server
   mission files."
  []
  (let [{:keys [single-path-fld
                single-path-lbl]} @state/controls
        mission-path (ui/get-text single-path-fld)]
    (when (not (string/blank? mission-path))
      (ui/set-label single-path-lbl mission-path))))

(defn single-choose-command
  "### single-choose-command
   This zero argument function gets the mission file selected by the user
   after displaying a mission file chooser. This value is relativised
   against the Missions directory so that it is in the format expected
   by the server in LOAD commands.

   This value is loaded into the single mission path label as the active
   mission to load."
  []
  (let [{:keys [mis-chooser
                single-path-lbl]} @state/controls
        file (ui/show-chooser mis-chooser)]
    (when file
      (ui/set-label single-path-lbl
                    (get-relative-path @state/server-path (.getCanonicalPath file))))))

(defn single-path-select
  "### server-path-select
   This zero argument function sets the global single mission path atom.
   It is called when the single mission path label text changes."
  []
  (let [{:keys [single-path-lbl]} @state/controls
        single-path (ui/get-text single-path-lbl)]
    (if (= single-path "...")
      (reset! state/mission-path nil)
      (reset! state/mission-path single-path))))

(defn set-server-selected
  "### set-server-selected
   This watch function sets the UI accordingly when a path to the server
   .exe has been defined."
  [_ _ _ path]
  (ui/set-ui-server path @state/controls)
  (ui/set-mis-dir path @state/controls))

(defn set-mis-selected
  "### set-mis-selected
   This watch function sets the UI accordingly when a single mission has
   been selected."
  [_ _ _ selected]
  (ui/set-ui-mis selected @state/connected @state/loaded @state/controls))

(defn save-ui-state
  "### save-ui-state
   This zero argument function saves the text values from various UI
   controls into settings atoms."
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