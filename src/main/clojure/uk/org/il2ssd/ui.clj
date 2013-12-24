;;;;
;;;; View update and input handling functions
;;;;
(ns uk.org.il2ssd.ui

    (:require [clojure.core.async :refer [go thread <! <!!]]
              [uk.org.il2ssd.channel :refer :all]
              [uk.org.il2ssd.jfx :as jfx]
              [uk.org.il2ssd.socket :as socket])

    (:import (javafx.application Platform)
             (javafx.scene Scene)
             (javafx.scene.input KeyEvent)
             (javafx.scene.text Font)
             (javafx.stage Stage WindowEvent)
             (uk.org.il2ssd MainView MainPresenter)))

(def controls (atom nil))
(def modes ["Single Mission" "Mission Cycle" "DCG Generation"])

(defn nothing []
    (jfx/event-handler [_] ()))

(defn close []
    (jfx/event-handler [windowevent] (do (.consume windowevent)
                                         (println "Exiting...")
                                         (Platform/exit))))

(defn update-console []
    (let [{:keys [console]} @controls]
        (thread (while @socket/connected
                    (let [text (<!! cln-channel)]
                        (if (not= text nil)
                            (jfx/run-later (.appendText console text))))))))

(defn enter-command []
    (jfx/event-handler [keyevent] (let [{:keys [cmd-entry]} @controls]
                                      (if (= (.. keyevent getCode getName) "Enter")
                                          (if (= (.getText cmd-entry) "clear")
                                              (.clear cmd-entry)
                                              (do (socket/write-socket (.getText cmd-entry))
                                                  (.clear cmd-entry)))))))

(defn connect-command []
    (jfx/event-handler [_] (let [{:keys [connect-btn disconn-btn console ip-field port-field]} @controls]
                               (go (socket/connect (.getText ip-field) (Integer/decode (.getText port-field)))
                                   (.setDisable connect-btn true)
                                   (.setDisable disconn-btn false)
                                   (.clear console)
                                   (socket/read-service)
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
            :about-btn (.getAboutItem presenter))))

(defn init-handlers []
    (let [{:keys [connect-btn
                  disconn-btn
                  start-btn
                  next-btn
                  cmd-entry
                  mode-choice
                  exit-btn
                  about-btn]}
          @controls]
        (.setOnAction connect-btn (connect-command))
        (.setOnAction disconn-btn (disconnect-command))
        (.setOnAction start-btn (nothing))
        (.setOnAction next-btn (nothing))
        (.setOnAction exit-btn (close))
        (.setOnKeyPressed cmd-entry (enter-command))))

(defn init-controls []
    (let [{:keys [mode-choice]} @controls]
        (doto mode-choice
            (.. getItems (addAll modes))
            (.. getSelectionModel selectFirst))))