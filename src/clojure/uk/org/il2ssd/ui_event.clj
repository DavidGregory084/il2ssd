;;;;
;;;; UI input event handling and channel listeners
;;;;
(ns uk.org.il2ssd.ui-event

  (:require [clojure.core.async :refer [go thread <!!]]
            [clojure.set :refer [map-invert]]
            [clojure.string :as string]
            [uk.org.il2ssd.channel :refer :all]
            [uk.org.il2ssd.jfx :as jfx]
            [uk.org.il2ssd.parse :as parse]
            [uk.org.il2ssd.server :as server]
            [uk.org.il2ssd.settings :as settings]
            [uk.org.il2ssd.state :as state])

  (:import (javafx.application Platform)
           (javafx.stage Stage FileChooser)
           (uk.org.il2ssd DifficultySetting SingleView)
           (javafx.collections ObservableList)
           (javafx.scene.control TextArea Button TextField TableView Label ChoiceBox TabPane)
           (javafx.scene.input KeyEvent)
           (javafx.scene.layout BorderPane)
           (javafx.beans InvalidationListener)
           (javafx.beans.value ChangeListener)
           (javafx.event EventHandler ActionEvent)
           (java.io File)))

(defn nothing
  "Dummy event handler function."
  []
  (jfx/event-handler [_] ()))

(defn close
  "Event handler function to save settings and close the program cleanly."
  []
  (jfx/event-handler [windowevent] (do (if @state/connected (server/disconnect))
                                       (settings/save-config-file)
                                       (Platform/exit))))

(defn about
  "Event handler function to open the About popup."
  []
  (jfx/event-handler [_] ()))

(defn start-stop-command
  "Event handler function to send the mission start command if stopped and the mission end command if running."
  []
  (jfx/event-handler [_] (if @state/playing
                           (server/end-mission)
                           (server/start-mission))))

(defn load-unload-command
  "Event handler function to load the selected mission or unload a loaded mission."
  []
  (jfx/event-handler [_] (if @state/loaded
                           (server/unload-mission))))

(defn get-difficulties
  "Event handler function to clear the difficulty settings table and request all difficulty settings from the server."
  []
  (jfx/event-handler [_] (let [{:keys [^ObservableList diff-data]} @state/controls]
                           (.clear diff-data)
                           (server/get-difficulty))))

(defn set-difficulties
  "Set all server difficulties from the difficulty settings table."
  []
  (jfx/event-handler [_] (let [{:keys [diff-data]} @state/controls]
                           (do (doseq [^DifficultySetting item diff-data]
                                 (let [setting (.getSetting item)
                                       value (.getValue item)]
                                   (server/set-difficulty setting value)))))))

(defn console-listener
  []
  (thread (while @state/connected
            (let [text (<!! print-channel)]
              (if text
                (let [{:keys [^TextArea console]} @state/controls]
                  (jfx/run-later (.appendText console text))))))))


(defn difficulty-listener
  []
  (thread (while @state/connected
            (let [text (<!! diff-channel)
                  {:keys [^ObservableList diff-data]} @state/controls]
              (if text
                (let [parsed (parse/difficulty-parser text)
                      [[_ setting] [_ value]] parsed]
                  (.add diff-data (DifficultySetting. setting value))))))))

(defn set-title
  "Multiple-arity function to set the stage title; with no arguments it resets the title to default.
  When called with the optional mission and state arguments it adds this information to the title."
  ([]
   (.setTitle ^Stage @state/stage "Il-2 Simple Server Daemon"))
  ([mission state]
   (.setTitle ^Stage @state/stage (str "Il-2 Simple Server Daemon - " mission " " (string/lower-case state)))))

(defn mission-listener
  []
  (thread (while @state/connected
            (let [text (<!! mis-channel)]
              (if text
                (let [parsed (parse/mission-parser text)]
                  (when (= 3 (count parsed))
                    (let [[[_ path] [_ mission] [_ state]] parsed]
                      (when (= state "Playing")
                        (do (reset! state/loaded true)
                            (reset! state/playing true)))
                      (when (= state "Loaded")
                        (do (reset! state/loaded true)
                            (reset! state/playing false)))
                      (jfx/run-later (set-title mission state))))
                  (when (= 1 (count parsed))
                    (let [[[_ state]] parsed]
                      (when (= state "NOT loaded")
                        (reset! state/loaded false)
                        (reset! state/playing false)
                        (jfx/run-later (set-title)))))))))))

(defn start-listeners
  []
  (console-listener)
  (difficulty-listener)
  (mission-listener))

(defn set-connected
  "Sets the UI controls for connected or disconnected status accordingly."
  [_ _ _ connected]
  (let [{:keys [^Button connect-btn
                ^Button disconn-btn
                ^ObservableList diff-data
                ^Button load-btn
                ^Button get-diff-btn
                ^Button set-diff-btn
                ^TextField cmd-entry
                ^TextArea console]}
        @state/controls]
    (if connected
      (do
        (start-listeners)
        (jfx/run-later (do (.setDisable connect-btn true)
                           (.setDisable disconn-btn false)
                           (.setDisable get-diff-btn false)
                           (.setDisable set-diff-btn false)
                           (.setDisable cmd-entry false)
                           (.clear console))))
      (do
        (.clear diff-data)
        (jfx/run-later (do (.setDisable connect-btn false)
                           (.setDisable disconn-btn true)
                           (.setDisable load-btn true)
                           (.setDisable get-diff-btn true)
                           (.setDisable set-diff-btn true)
                           (.setDisable cmd-entry true)
                           (.setText console "<disconnected>")))))))

(defn set-mission-playing
  "Sets the controls for playing status accordingly."
  [_ _ _ playing]
  (let [{:keys [^TableView diff-table
                ^Button set-diff-btn
                ^Button start-btn]} @state/controls]
    (if playing
      (jfx/run-later (do (.setEditable diff-table false)
                         (.setDisable set-diff-btn true)
                         (.setText start-btn "\uf04d Stop")))
      (jfx/run-later (do (.setEditable diff-table true)
                         (.setDisable set-diff-btn false)
                         (.setText start-btn "\uf04b Start"))))))

(defn set-mission-loaded
  "Sets the UI controls for loaded status accordingly."
  [_ _ _ loaded]
  (let [{:keys [^Button start-btn
                ^Button load-btn]} @state/controls]
    (if loaded
      (jfx/run-later (do (.setDisable start-btn false)
                         (.setDisable load-btn false)
                         (.setText load-btn "\uf05e Unload")))
      (jfx/run-later (do (.setDisable start-btn true)
                         (.setText load-btn "\uf093 Load")
                         (if @state/mis-selected
                           (.setDisable load-btn false)
                           (.setDisable load-btn true)))))))


(defn enter-command
  "Event handler function to write user input to the server console and optionally clear the console when 'clear' is typed."
  []
  (jfx/event-handler
    [^KeyEvent keyevent]
    (let [{:keys [^TextField cmd-entry
                  ^TextArea console]} @state/controls]
      (if (= (-> keyevent .getCode .getName) "Enter")
        (if (= (.getText cmd-entry) "clear")
          (do (.clear console)
              (.clear cmd-entry))
          (do (server/write-socket (.getText cmd-entry))
              (.clear cmd-entry)))))))

(defn connect-command
  []
  (jfx/event-handler
    [_]
    (let [{:keys [^TextField ip-field
                  ^TextField port-field]} @state/controls]
      (go (server/connect ^String (.getText ip-field) (Integer/decode ^String (.getText port-field)))))))

(defn disconnect-command
  []
  (jfx/event-handler [_] (go (server/disconnect))))

(defn field-exit
  ^ChangeListener []
  (jfx/change-listener
    [property old new]
    (let [{:keys [^TextField ip-field
                  ^TextField port-field]} @state/controls]
      (if (not new)
        (settings/save-server (.getText ip-field) (.getText port-field))))))

(defn changed-choice
  ^InvalidationListener [modes]
  (jfx/invalidation-listener
    [property]
    (let [{:keys [^BorderPane mission-pane
                  ^BorderPane single-mis-pane
                  ^ChoiceBox mode-choice
                  ^Label server-path-lbl]} @state/controls
          server-path (.getText server-path-lbl)
          mode (name ((map-invert modes) (.getValue mode-choice)))]
      (when (= mode "single")
        (.setCenter mission-pane single-mis-pane))
      (settings/save-mission mode)
      (settings/save-server server-path))))

(defn server-choose-command
  []
  (jfx/event-handler
    [_]
    (let [{:keys [^FileChooser server-chooser
                  ^Label server-path-lbl]} @state/controls
          ^File file (.showOpenDialog server-chooser (Stage.))
          path (.getCanonicalPath file)]
      (.setText server-path-lbl path))))
