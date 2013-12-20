;;;;
;;;; View update functions
;;;;
(ns uk.org.il2ssd.ui

    (:require [uk.org.il2ssd.jfx :as jfx]
              [uk.org.il2ssd.event :as event])

    (:import (javafx.scene Scene)
             (javafx.scene.image Image)
             (javafx.scene.text Font)
             (javafx.stage Stage)
             (uk.org.il2ssd MainView MainPresenter)))
	
(def controls (atom nil))

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
              (.setOnCloseRequest (event/close)))
        (.getPresenter view)))

(defn init-objects [presenter]
    (->> (hash-map :connect-btn (.getConnectButton presenter)
        :disconn-btn (.getDisconnectButton presenter)
        :prog-ind (.getProgressIndicator presenter)
        :start-btn (.getStartStopButton presenter)
        :next-btn (.getNextButton presenter)
        :cmd-entry (.getCommandEntryField presenter)
        :console (.getConsoleTextArea presenter)
        :mode-choice (.getMissionModeChoice presenter)
        :exit-btn (.getExitItem presenter)
        :about-btn (.getAboutItem presenter))
		(reset! controls))
		controls)

(defn init-handlers [control-map]
    (let [{:keys [connect-btn
                 disconn-btn
                 prog-ind
                 start-btn
                 next-btn
                 cmd-entry
                 console
                 mode-choice
                 exit-btn
                 about-btn]}
          @control-map]
        (.setOnAction connect-btn (event/nothing))
        (.setOnAction disconn-btn (event/nothing))
        (.setOnAction start-btn (event/nothing))
        (.setOnAction next-btn (event/nothing))
        (.setOnAction exit-btn (event/close))
        (.setOnKeyPressed cmd-entry (event/enter-command))
        control-map))


