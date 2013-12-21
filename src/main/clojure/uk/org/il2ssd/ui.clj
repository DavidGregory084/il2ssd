;;;;
;;;; View update and input handling functions
;;;;
(ns uk.org.il2ssd.ui

    (:require [uk.org.il2ssd.jfx :as jfx])

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

(defn enter-command []
    (jfx/event-handler [keyevent] (let [{:keys [cmd-entry]} @controls]
                                      (if (= (.. keyevent getCode getName) "Enter")
                                          (do (println (.getText cmd-entry))
                                              (.clear cmd-entry))))))

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
        (.setOnAction connect-btn (nothing))
        (.setOnAction disconn-btn (nothing))
        (.setOnAction start-btn (nothing))
        (.setOnAction next-btn (nothing))
        (.setOnAction exit-btn (close))
        (.setOnKeyPressed cmd-entry (enter-command))))

(defn init-controls []
    (let [{:keys [mode-choice]} @controls]
        (doto mode-choice
            (.. getItems (addAll modes))
            (.. getSelectionModel selectFirst))))