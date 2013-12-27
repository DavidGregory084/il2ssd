;;;;
;;;; UI initialisation
;;;;
(ns uk.org.il2ssd.ui-init

    (:require [uk.org.il2ssd.channel :refer :all]
              [uk.org.il2ssd.settings :as settings]
              [uk.org.il2ssd.server :as server]
              [uk.org.il2ssd.state :as state]
              [uk.org.il2ssd.ui-event :as event])

    (:import (javafx.scene Scene)
             (javafx.scene.text Font)
             (uk.org.il2ssd MainView MainPresenter)))

(defn init-stage [primaryStage]
    (let [stage primaryStage
          view (MainView.)
          scene (Scene. (.getView view))]
        (reset! state/stage primaryStage)
        (event/set-title)
        (Font/loadFont "fontawesome-webfont.ttf" 12.0)
        (settings/read-from-file)
        (doto stage
            (.setScene scene)
            (.setResizable false)
            (.show)
            (.setOnCloseRequest (event/close)))
        (.getPresenter view)))

(defn init-objects [presenter]
    (reset! state/controls
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
          @state/controls]
        (.setOnAction connect-btn (event/connect-command))
        (.setOnAction disconn-btn (event/disconnect-command))
        (.setOnAction start-btn (event/start-stop-command))
        (.setOnAction next-btn (event/nothing))
        (.setOnAction exit-btn (event/close))
        (.setOnAction get-diff-btn (event/get-difficulties))
        (.setOnAction set-diff-btn (event/set-difficulties))
        (.setOnKeyPressed cmd-entry (event/enter-command))
        (.. ip-field focusedProperty (addListener (event/changed-state)))
        (.. port-field focusedProperty (addListener (event/changed-state)))
        (.. mode-choice valueProperty (addListener (event/invalid-state)))))

(defn init-controls []
    (let [{:keys [mode-choice]} @state/controls]
        (let [{:keys [mode]} @settings/mission-settings]
            (doto mode-choice
                (.. getItems (addAll (map state/modes [:single :cycle :dcg])))
                (.. getSelectionModel selectFirst)))))