;;;;
;;;; View update and input handling functions
;;;;
(ns uk.org.il2ssd.ui

    (:require [clojure.core.async :refer [go thread <!!]]
              [uk.org.il2ssd.channel :refer :all]
              [uk.org.il2ssd.jfx :as jfx]
              [uk.org.il2ssd.settings :as settings]
              [uk.org.il2ssd.socket :as socket]
              [uk.org.il2ssd.state :as state])

    (:import (javafx.application Platform)
             (javafx.scene Scene)
             (javafx.scene.input KeyEvent)
             (javafx.scene.text Font)
             (javafx.stage Stage WindowEvent)
             (uk.org.il2ssd DifficultySetting MainView MainPresenter)))

(def controls (atom nil))
(def modes {:single "Single Mission", :cycle "Mission Cycle", :dcg "DCG Generation"})

(defn nothing []
    (jfx/event-handler [_] ()))

(defn changed-state []
    (jfx/change-listener [property old new]
        (let [property-type (type property)
              {:keys [ip-field port-field]} @controls]
            (if (not new)
                (println "focus lost")))))

(defn invalid-state []
    (jfx/invalidation-listener [property]
        (let [property-type (type property)
              {:keys [mode-choice]} @controls]
            (println "changed"))))

(defn close []
    (jfx/event-handler [windowevent] (do (.consume windowevent)
                                         (println "Exiting...")
                                         (settings/save-to-file)
                                         (if @state/connected
                                             (socket/disconnect))
                                         (Platform/exit))))

(defn about []
    (jfx/event-handler [_] ()))

(defn get-difficulties []
    (jfx/event-handler [_] (let [{:keys [diff-data]} @controls
                                 diffs @settings/difficulties]
                               (.clear diff-data)
                               (doseq [item diffs]
                                   (let [[setting value] item]
                                       (.add diff-data (DifficultySetting. setting value)))))))

(defn set-difficulties []
    (jfx/event-handler [_] (let [{:keys [diff-data]} @controls]
                               (doseq [item diff-data]
                                   (let [setting (.getSetting item)
                                         value (.getValue item)]
                                       (socket/set-difficulty setting value)))
                               (socket/get-difficulty))))

(defn update-console []
    (let [{:keys [console]} @controls]
        (thread (while @state/connected
                    (let [text (<!! print-channel)]
                        (if (not= text nil)
                            (jfx/run-later (.appendText console text))))))))

(defn enter-command []
    (jfx/event-handler [keyevent] (let [{:keys [cmd-entry console]} @controls]
                                      (if (= (.. keyevent getCode getName) "Enter")
                                          (if (= (.getText cmd-entry) "clear")
                                              (do (.clear console)
                                                  (.clear cmd-entry))
                                              (do (socket/write-socket (.getText cmd-entry))
                                                  (.clear cmd-entry)))))))

(defn connect-command []
    (jfx/event-handler [_] (let [{:keys [connect-btn disconn-btn console ip-field port-field]} @controls]
                               (go (socket/connect (.getText ip-field) (Integer/decode (.getText port-field)))
                                   (.setDisable connect-btn true)
                                   (.setDisable disconn-btn false)
                                   (.clear console)
                                   (update-console)))))

(defn disconnect-command []
    (jfx/event-handler [_] (let [{:keys [connect-btn disconn-btn console]} @controls]
                               (go (socket/disconnect)
                                   (.clear console)
                                   (.setDisable disconn-btn true)
                                   (.setDisable connect-btn false)
                                   (.setText console "<disconnected>")))))

(defn init-stage [primaryStage]
    (let [stage primaryStage
          view (MainView.)
          scene (Scene. (.getView view))]
        (Font/loadFont "fontawesome-webfont.ttf" 12.0)
        (settings/load-from-file)
        (doto stage
            (.setTitle "Il-2 Simple Server Daemon")
            (.setScene scene)
            (.setResizable false)
            (.show)
            (.setOnCloseRequest (close)))
        (.getPresenter view)))

(defn init-objects [presenter]
    (reset! controls
        (hash-map :connect-btn (.getConnectButton presenter)
            :disconn-btn (.getDisconnectButton presenter)
            :prog-ind (.getProgressIndicator presenter)
            :start-btn (.getStartStopButton presenter)
            :next-btn (.getNextButton presenter)
            :cmd-entry (.getCommandEntryField presenter)
            :console (.getConsoleTextArea presenter)
            :mode-choice (.getMissionModeChoice presenter)
            :ip-field (.getIpAddressField presenter)
            :port-field (.getPortField presenter)
            :exit-btn (.getExitItem presenter)
            :about-btn (.getAboutItem presenter)
            :get-diff-btn (.getGetDifficultyButton presenter)
            :set-diff-btn (.getSetDifficultyButton presenter)
            :diff-table (.getDifficultyTable presenter)
            :diff-data (.getDifficultyData presenter))))

(defn init-handlers []
    (let [{:keys [connect-btn
                  disconn-btn
                  start-btn
                  next-btn
                  cmd-entry
                  mode-choice
                  exit-btn
                  about-btn
                  get-diff-btn
                  set-diff-btn
                  ip-field
                  port-field]}
          @controls]
        (.setOnAction connect-btn (connect-command))
        (.setOnAction disconn-btn (disconnect-command))
        (.setOnAction start-btn (nothing))
        (.setOnAction next-btn (nothing))
        (.setOnAction exit-btn (close))
        (.setOnAction get-diff-btn (get-difficulties))
        (.setOnAction set-diff-btn (set-difficulties))
        (.setOnKeyPressed cmd-entry (enter-command))
        (.. ip-field focusedProperty (addListener (changed-state)))
        (.. port-field focusedProperty (addListener (changed-state)))
        (.. mode-choice valueProperty (addListener (invalid-state)))))

(defn init-controls []
    (let [{:keys [mode-choice]} @controls]
        (let [{:keys [mode]} @settings/mission]
            (doto mode-choice
                (.. getItems (addAll (map modes [:single :cycle :dcg])))
                (.. getSelectionModel selectFirst)))))