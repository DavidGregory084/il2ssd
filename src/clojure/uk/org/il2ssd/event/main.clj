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

(defn start-stop-cycle-command
  "### start-stop-cycle-command
   This function stops the cycle if it is running, or starts it if it is not.
   In either case it stops any mission that is playing."
  []
  (if @state/cycle-running
    (do (cycle/stop-cycle)
        (when @state/playing
          (server/unload-mission)))
    (do (when @state/playing
          (server/unload-mission))
        (cycle/start-cycle))))

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
    (do (ui/toggle-prog-ind @state/control-instances true)
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
                    {:keys [diff-data]} @state/control-instances
                    {:keys [setting value]} parsed]
                (ui/add-diff-data diff-data setting value))))))

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
              (ui/toggle-prog-ind @state/control-instances false)
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
  (difficulty-listener)
  (mission-listener)
  (error-listener))

(defn save-ui-state
  "### save-ui-state
   This zero argument function saves the text values from various UI
   controls into settings atoms."
  []
  (let [{:keys [ip-field
                port-field
                server-path-lbl
                single-path-lbl
                cycle-data]} @state/control-instances
        mode @state/mode
        ip-addr (ui/get-text ip-field)
        port (ui/get-text port-field)
        server-path (ui/get-text server-path-lbl)
        single-path (ui/get-text single-path-lbl)]
    (config/save-server ip-addr port server-path)
    (config/save-mission mode single-path)
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
      (when @state/connected (server/disconnect))
      (close-channels)
      (save-ui-state)
      (config/save-config-file)
      (ui/exit)))

(defn next-command
  "### next-command
   This function calls the next cycle mission function, specifying that this
   load event is unscheduled."
  []
  (when (= @state/mode "cycle")
    (cycle/next-mission false)))

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
    :loaded (do (ui/toggle-load-txt new @state/control-instances)
                (ui/toggle-prog-ind @state/control-instances false))
    :playing (ui/toggle-start-txt new @state/control-instances)
    :server-path (ui/set-mis-dir new @state/control-instances)
    :single-mission-path nil
    :cycle-mission-path nil
    :cycle-running (ui/toggle-cycle-start-txt new @state/control-instances))
  (let [new-state (assoc (state) key new)]
    (ui/set-button-state new-state controls)))