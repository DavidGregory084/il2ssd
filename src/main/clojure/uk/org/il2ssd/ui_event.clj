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

    (:import [javafx.application Platform]
             [javafx.stage Stage]
             [uk.org.il2ssd DifficultySetting SingleView]))

(defn nothing []
    (jfx/event-handler [_] ()))

(defn close []
    (jfx/event-handler [windowevent] (do (if @state/connected (server/disconnect))
                                         (settings/save-to-file)
                                         (Platform/exit))))

(defn about []
    (jfx/event-handler [_] ()))

(defn start-stop-command []
    (jfx/event-handler [_] (if @state/playing
                               (server/end-mission)
                               (server/start-mission))))

(defn load-unload-command []
    (jfx/event-handler [_] (if @state/loaded
                               (server/unload-mission))))

(defn get-difficulties []
    (jfx/event-handler [_] (let [{:keys [diff-data]} @state/controls
                                 diffs @settings/difficulty-settings]
                               (.clear diff-data)
                               (doseq [item diffs]
                                   (let [[setting value] item]
                                       (.add diff-data (DifficultySetting. setting value)))))))

(defn set-difficulties []
    (jfx/event-handler [_] (let [{:keys [diff-data]} @state/controls]
                               (doseq [item diff-data]
                                   (let [setting (.getSetting item)
                                         value (.getValue item)]
                                       (server/set-difficulty setting value)))
                               (server/get-difficulty))))

(defn get-saved-difficulties [path]
    (let [saved-diffs (settings/read-difficulty-file path)]
        (doseq [line (saved-diffs "Difficulty")])))

(defn set-saved-difficulties []
    (doseq [item @settings/saved-difficulties]
        (let [[setting value] item]
            (server/set-difficulty setting value)))
    (server/get-difficulty))

(defn set-title
    ([]
        (reset! state/title ["Il-2 Simple Server Daemon"])
        (.setTitle @state/stage (string/join " - " @state/title)))
    ([mission state]
        (reset! state/title ["Il-2 Simple Server Daemon"])
        (swap! state/title conj (str mission " " (string/lower-case state)))
        (.setTitle @state/stage (string/join " - " @state/title))))

(defn set-connected []
    (let [{:keys [connect-btn
                  disconn-btn
                  diff-data
                  load-btn
                  get-diff-btn
                  set-diff-btn
                  cmd-entry
                  console]}
          @state/controls]
        (if @state/connected
            (do (.setDisable connect-btn true)
                (.setDisable disconn-btn false)
                (.setDisable get-diff-btn false)
                (.setDisable set-diff-btn false)
                (.setDisable cmd-entry false)
                (.clear console))
            (do (.setDisable connect-btn false)
                (.setDisable disconn-btn true)
                (.setDisable load-btn true)
                (.setDisable get-diff-btn true)
                (.setDisable set-diff-btn true)
                (.setDisable cmd-entry true)
                (.clear diff-data)
                (.setText console "<disconnected>")))))

(defn set-mission-controls []
    (let [{:keys [diff-table set-diff-btn start-btn load-btn]} @state/controls]
        (if @state/playing
            (do (.setEditable diff-table false)
                (.setDisable set-diff-btn true)
                (.setText start-btn "\uf04d Stop"))
            (do (.setEditable diff-table true)
                (.setDisable set-diff-btn false)
                (.setText start-btn "\uf04b Start")))
        (if @state/loaded
            (do (.setDisable start-btn false)
                (.setDisable load-btn false)
                (.setText load-btn "\uf05e Unload"))
            (do (.setDisable start-btn true)
                (.setText load-btn "\uf093 Load")
                (if @state/mis-selected
                    (.setDisable load-btn false)
                    (.setDisable load-btn true))))))

(defn console-listener []
    (thread (while @state/connected
                (let [text (<!! print-channel)]
                    (if text
                        (let [{:keys [console]} @state/controls]
                            (jfx/run-later (.appendText console text))))))))


(defn difficulty-listener []
    (thread (while @state/connected
                (let [text (<!! diff-channel)]
                    (if text
                        (let [parsed (parse/difficulty-parser text)
                              [[_ setting] [_ number]] parsed]
                            (swap! settings/difficulty-settings assoc setting number)))))))

(defn mission-listener []
    (thread (while @state/connected
                (let [text (<!! mis-channel)]
                    (if text
                        (let [parsed (parse/mission-parser text)]
                            (when (= 3 (count parsed))
                                (let [[[_ path] [_ mission] [_ state]] parsed]
                                    (when (= state "Playing")
                                        (do (reset! state/loaded true)
                                            (reset! state/playing true)
                                            (jfx/run-later (set-mission-controls))))
                                    (when (= state "Loaded")
                                        (do (reset! state/loaded true)
                                            (reset! state/playing false)
                                            (jfx/run-later (set-mission-controls))))
                                    (jfx/run-later (set-title mission state))))
                            (when (= 1 (count parsed))
                                (let [[[_ state]] parsed]
                                    (when (= state "NOT loaded")
                                        (reset! state/loaded false)
                                        (reset! state/playing false)
                                        (jfx/run-later
                                            (do (set-mission-controls)
                                                (set-title))))))))))))

(defn start-listeners []
    (console-listener)
    (difficulty-listener)
    (mission-listener))

(defn enter-command []
    (jfx/event-handler [keyevent] (let [{:keys [cmd-entry console]} @state/controls]
                                      (if (= (.. keyevent getCode getName) "Enter")
                                          (if (= (.getText cmd-entry) "clear")
                                              (do (.clear console)
                                                  (.clear cmd-entry))
                                              (do (server/write-socket (.getText cmd-entry))
                                                  (.clear cmd-entry)))))))

(defn connect-command []
    (jfx/event-handler [_] (let [{:keys [ip-field port-field]} @state/controls]
                               (go (server/connect (.getText ip-field) (Integer/decode (.getText port-field)))
                                   (jfx/run-later
                                       (do (set-connected)
                                           (set-mission-controls)))
                                   (start-listeners)))))

(defn disconnect-command []
    (jfx/event-handler [_] (go (server/disconnect)
                               (jfx/run-later
                                   (do (set-mission-controls)
                                       (set-title)
                                       (set-connected))))))

(defn field-exit []
    (jfx/change-listener [property old new]
        (let [{:keys [ip-field port-field]} @state/controls]
            (if (not new)
                (settings/save-server (.getText ip-field) (.getText port-field))))))

(defn changed-choice []
    (jfx/invalidation-listener [property]
        (let [{:keys [mission-pane single-mis-pane mode-choice server-path-lbl]} @state/controls
              srv-pth (.getText server-path-lbl)
              mode (name ((map-invert state/modes) (.getValue mode-choice)))]
            (when (= mode "single")
                (.setCenter mission-pane single-mis-pane))
            (reset! state/mode mode)
            (reset! state/server-path srv-pth)
            (settings/save-mission @state/mode)
            (settings/save-server @state/server-path))))

(defn server-choose-command []
    (jfx/event-handler [_]
        (let [{:keys [server-chooser server-path-lbl]} @state/controls
              file (.showOpenDialog server-chooser (Stage.))
              path (.getCanonicalPath file)]
            (.setText server-path-lbl path))))
