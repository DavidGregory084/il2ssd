(ns uk.org.il2ssd.event.main
  (:require [clojure.core.async :refer [<!! go thread]]
            [clojure.string :as string]
            [uk.org.il2ssd.channel :refer :all]
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

(defn start-stop-command
  "### start-stop-command
   This is a zero argument function which ends the current mission if is playing,
   or starts the currently loaded mission if it is not."
  []
  (when (= @state/mode "single")
    (if @state/playing
      (server/end-mission)
      (server/start-mission))))

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
    (config/save-server ip-addr port server-path)
    (config/save-mission mode single-path)))

  (defn close
    "### close
     This is a zero argument function which disconnects from the server if the
     program is connected, saves the current settings to the config file and
     requests to close the program."
    []
    (do (if @state/connected (server/disconnect))
        (save-ui-state)
        (config/save-config-file)
        (ui/exit)))