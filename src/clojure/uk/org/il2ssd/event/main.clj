(ns uk.org.il2ssd.event.main
  (:require [clojure.core.async :refer [<!! go thread]]
            [clojure.string :as string]
            [uk.org.il2ssd.channel :refer :all]
            [uk.org.il2ssd.event.cycle :as cycle]
            [uk.org.il2ssd.event.mission :as mission]
            [uk.org.il2ssd.jfx.ui :as ui]
            [uk.org.il2ssd.parse :refer :all]
            [uk.org.il2ssd.server :as server]
            [uk.org.il2ssd.config :as config]
            [uk.org.il2ssd.state :as state]))

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
                port-field]} @state/control-instances
        ip (ui/get-text ip-field)
        port (ui/get-text port-field)]
    (go (try (server/connect ip (Integer/decode port))
             (catch NumberFormatException _ nil)
             (catch NullPointerException _ nil)))))

(defn disconnect-command
  "### disconnect-command
   This is a zero argument function which calls the server disconnect function
   in a go block so that it doesn't block the calling thread."
  []
  (when @state/cycle-running
    (cycle/stop-cycle))
  (go (server/disconnect)))

(defn start-stop-command
  "### start-stop-command
   This is a zero argument function which ends the current mission if is playing,
   or starts the currently loaded mission if it is not.
   If a cycle is running it stops the cycle first."
  []
  (when @state/cycle-running
    (cycle/stop-cycle))
  (if @state/playing
    (server/end-mission)
    (server/start-mission)))

(defn load-unload-command
  "### load-unload-command
   This is a zero argument function which unloads the currently loaded mission if
   it is loaded, and loads the current mission if it is not.
   If a cycle is running, it stops the cycle first."
  []
  (when @state/cycle-running
    (cycle/stop-cycle))
  (if @state/loaded
    (server/unload-mission)
    (do (reset! state/loading true)
        (server/load-mission @state/single-mission-path))))

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
              (let [{:keys [console]} @state/control-instances]
                (ui/print-console console text))))))

(defn pilot-listener
  []
  (thread (while @state/connected
            (when-let [text (<!! pilot-channel)]
              (let [parsed (parse-text pilot-parser text)
                    {:keys [pilots-data]} @state/control-instances
                    {:keys [socket ip name]} parsed]
                (if name
                  (do (ui/add-pilot-data pilots-data socket ip name)
                      (server/get-user-details name))
                  (ui/remove-pilot-data pilots-data socket)))))))

(defn ban-listener
  []
  (thread (while @state/connected
            (when-let [text (<!! ban-channel)]
              (when (= @state/last-command "ban")
                (let [ban (string/trim-newline (apply str (drop 2 text)))]
                  (if (re-matches #"(\d++{1,3}+\.?+){4}+" ban)
                    (println {:type "IP" :value ban})
                    (println {:type "Name" :value ban}))))))))

(defn user-listener
  []
  (thread (while @state/connected
            (when-let [text (<!! user-channel)]
              (when (re-matches #"user\s.+(?!STAT)" @state/last-command)
                (let [parsed (parse-text user-parser text)
                      {:keys [name armyname score]} parsed]
                  (println parsed)))))))

(defn host-listener
  []
  (thread (while @state/connected
            (when-let [text (<!! host-channel)]
              (when (= @state/last-command "host")
                (let [parsed (parse-text host-parser text)
                      {:keys [number name socket ip]} parsed]
                  (println parsed)))))))

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
              (when (= @state/last-command "difficulty")
                (let [parsed (parse-text difficulty-parser text)
                      {:keys [diff-data]} @state/control-instances
                      {:keys [setting value]} parsed]
                  (ui/add-diff-data diff-data setting value)))))))

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
              (reset! state/loading false)
              (let [parsed (parse-text mission-parser text)
                    {:keys [path mission state]} parsed]
                (if mission
                  (let [mis-path (str path mission)]
                    (when (= mis-path @state/dcg-mission-path)
                      (reset! state/dcg-running true))
                    (when (= state "Loaded")
                      (reset! state/loaded true)
                      (reset! state/playing false))
                    (when (= state "Playing")
                      (reset! state/loaded true)
                      (reset! state/playing true))
                    (set-title mission state))
                  (when (= state "NOT loaded")
                    (reset! state/dcg-running false)
                    (reset! state/loaded false)
                    (reset! state/playing false)
                    (set-title))))))))

(defn error-listener
  "### error-listener
   This is a zero argument function which listens for text on the error channel.

   This channel filters for mission loading errors, so when we receive a value
   from this channel we know that the requested mission failed to load, and we
   can set the mission state to unloaded.

   If a mission cycle is running, we know that the scheduled mission failed to
   load and we can skip this mission."
  []
  (thread (while @state/connected
            (when-let [_ (<!! err-channel)]
              (reset! state/loading false)
              (reset! state/loaded false)
              (reset! state/playing false)
              (set-title)
              (when @state/cycle-running
                (cycle/next-mission false))))))

(defn start-listeners
  "### start-listeners
   This is a zero argument convenience function which starts all of the listeners
   which parse the server console output.

   They all need to be running and removing puts from the channels or the program
   will stall - every tap must take each value from the mult to stay synchronised."
  []
  (console-listener)
  (pilot-listener)
  (difficulty-listener)
  (mission-listener)
  (error-listener)
  (ban-listener)
  (user-listener)
  (host-listener))

(defn save-ui-state
  "### save-ui-state
   This zero argument function saves the text values from various UI
   controls into settings atoms."
  []
  (let [{:keys [ip-field
                port-field
                server-path-lbl
                single-path-lbl
                dcg-path-lbl
                cycle-data]} @state/control-instances
        mode @state/mode
        ip-addr (ui/get-text ip-field)
        port (ui/get-text port-field)
        server-path (ui/get-text server-path-lbl)
        single-path (ui/get-text single-path-lbl)
        dcg-path (ui/get-text dcg-path-lbl)]
    (config/save-server ip-addr port server-path)
    (config/save-mission mode single-path)
    (config/save-dcg dcg-path)
    (doseq [item cycle-data
            :let [cycle-mission (ui/get-cycle-mission item)
                  mission (:mission cycle-mission)
                  timer (:timer cycle-mission)
                  index (ui/get-index-of-mission cycle-data item)]]
      (config/save-cycle index mission timer))))

(defn close
  "### close
   This is a zero argument function which disconnects from the server if the
   program is connected, saves the current settings to the config file and
   requests to close the program."
  []
  (do (when @state/cycle-running (cycle/stop-cycle))
      (shutdown-agents)
      (when @state/connected (server/disconnect))
      (close-channels)
      (save-ui-state)
      (config/save-config-file)
      (ui/exit)))

(defn update-ui
  "### update-ui
   This function is called whenever any watched global state atom is changed.
   The key provided specifies which state atom has changed.
   For each event, any UI processing specific to that event is triggered.
   To ensure that the state triggered is correct, the new state is assoc-ed
   into the map provided, then the button state is set correctly for the
   current global state based upon the dependencies defined in the controls
   map."
  [state controls key _ _ new]
  (case key
    :connected (do (ui/toggle-console-text new @state/control-instances)
                   (if new
                     (start-listeners)
                     (do (set-title)
                         (ui/clear-diff-data @state/control-instances))))
    :loading (ui/toggle-prog-ind new @state/control-instances)
    :loaded (ui/toggle-load-txt new @state/control-instances)
    :playing (ui/toggle-start-txt new @state/control-instances)
    :server-path (ui/set-mis-dir new @state/control-instances)
    :dcg-path nil
    :single-mission-path nil
    :cycle-mission-path nil
    :dcg-mission-path nil
    :cycle-running (do (ui/toggle-cycle-start-txt new @state/control-instances)
                       (when (not new)
                         (ui/highlight-table-row -1 @state/control-instances)))
    :cycle-index (ui/highlight-table-row new @state/control-instances)
    :dcg-running (ui/toggle-dcg-start-txt new @state/control-instances))
  (let [new-state (assoc (state) key new)]
    (ui/set-button-state new-state controls)))