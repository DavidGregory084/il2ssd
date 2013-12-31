;;;;
;;;; UI initialisation
;;;;
(ns uk.org.il2ssd.ui-init

  (:require [uk.org.il2ssd.channel :refer :all]
            [uk.org.il2ssd.settings :as settings]
            [uk.org.il2ssd.server :as server]
            [uk.org.il2ssd.state :as state]
            [uk.org.il2ssd.ui-event :as event])

  (:import (java.nio.file Paths)
           (javafx.scene Scene)
           (javafx.scene.layout HBox Priority)
           (javafx.scene.text Font)
           (javafx.stage FileChooser FileChooser$ExtensionFilter)
           (uk.org.il2ssd MainView MainPresenter SingleView SinglePresenter)))

(def modes
  "Map of mission loading modes."
  {:single "Single Mission", :cycle "Mission Cycle", :dcg "DCG Generation"})

(defn init-stage
  [primaryStage]
  (let [stage primaryStage
        main-view (MainView.)
        single-view (SingleView.)
        scene (Scene. (.getView main-view))]
    (reset! state/stage primaryStage)
    (event/set-title)
    (Font/loadFont "fontawesome-webfont.ttf" 12.0)
    (doto stage
      (.setScene scene)
      (.setResizable false)
      (.show)
      (.setOnCloseRequest (event/close)))
    [(.getPresenter main-view) (.getPresenter single-view)]))

(defn init-objects [presenters]
  (let [[main-presenter single-presenter] presenters]
    (reset! state/controls
            (hash-map :connect-btn (.getConnectButton main-presenter)
                      :disconn-btn (.getDisconnectButton main-presenter)
                      :prog-stack (.getProgressStack main-presenter)
                      :prog-ind (.getProgressIndicator main-presenter)
                      :start-btn (.getStartStopButton main-presenter)
                      :next-btn (.getNextButton main-presenter)
                      :cmd-entry (.getCommandEntryField main-presenter)
                      :console (.getConsoleTextArea main-presenter)
                      :mission-pane (.getMissionPane main-presenter)
                      :mode-choice (.getMissionModeChoice main-presenter)
                      :mission-spring (.getMissionBarSpring main-presenter)
                      :load-btn (.getMissionLoadButton main-presenter)
                      :ip-field (.getIpAddressField main-presenter)
                      :port-field (.getPortField main-presenter)
                      :exit-btn (.getExitItem main-presenter)
                      :about-btn (.getAboutItem main-presenter)
                      :server-path-lbl (.getServerPathLabel main-presenter)
                      :server-path-btn (.getServerPathButton main-presenter)
                      :get-diff-btn (.getGetDifficultyButton main-presenter)
                      :set-diff-btn (.getSetDifficultyButton main-presenter)
                      :diff-table (.getDifficultyTable main-presenter)
                      :diff-data (.getDifficultyData main-presenter)
                      :server-chooser (FileChooser.)
                      :mis-chooser (FileChooser.)
                      :dcg-chooser (FileChooser.)
                      :single-mis-pane (.getSingleMisPane single-presenter)))))

(defn init-handlers []
  (let [{:keys [connect-btn
                disconn-btn
                start-btn
                next-btn
                cmd-entry
                mode-choice
                load-btn
                exit-btn
                about-btn
                server-path-lbl
                server-path-btn
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
    (.setOnAction server-path-btn (event/server-choose-command))
    (.setOnAction get-diff-btn (event/get-difficulties))
    (.setOnAction set-diff-btn (event/set-difficulties))
    (.setOnKeyPressed cmd-entry (event/enter-command))
    (.setOnAction load-btn (event/load-unload-command))
    (.. ip-field focusedProperty (addListener (event/field-exit)))
    (.. port-field focusedProperty (addListener (event/field-exit)))
    (.. mode-choice valueProperty (addListener (event/changed-choice modes)))
    (.. server-path-lbl textProperty (addListener (event/changed-choice modes)))
    (add-watch state/connected :connect event/set-connected)
    (add-watch state/loaded :load event/set-mission-loaded)
    (add-watch state/playing :play event/set-mission-playing)))

(defn init-controls []
  (let [{:keys [ip-field
                port-field
                prog-stack
                mode-choice
                mission-spring
                server-chooser
                server-path-lbl
                mis-chooser
                dcg-chooser]} @state/controls
        configuration (settings/read-config-file)]
    (.. mode-choice getItems (addAll (map modes [:single :cycle :dcg])))
    (if configuration
      (do (.. mode-choice getSelectionModel
              (select
                ((comp modes keyword get-in) configuration ["Mission" "Mode"] "single")))
          (.setText ip-field (get-in configuration ["Server" "IP"] ""))
          (.setText port-field (get-in configuration ["Server" "Port"] ""))
          (.setText server-path-lbl (get-in configuration ["Server" "Path"] ""))
          (settings/save-server (.getText ip-field) (.getText port-field) (.getText server-path-lbl)))
      (.. mode-choice getSelectionModel selectFirst))
    (HBox/setHgrow prog-stack Priority/ALWAYS)
    (HBox/setHgrow mission-spring Priority/ALWAYS)
    (doto server-chooser
      (.setTitle "Choose Il-2 Server Executable")
      (.setInitialDirectory
        (.. (Paths/get "" (into-array [""])) toAbsolutePath toFile))
      (.. getExtensionFilters
          (add
            (FileChooser$ExtensionFilter. "Il-2 Server (il2server.exe)" (into-array ["il2server.exe"])))))
    (doto mis-chooser
      (.setTitle "Choose Il-2 Mission File")
      (.. getExtensionFilters
          (add
            (FileChooser$ExtensionFilter. "Il-2 Mission (*.mis)" (into-array ["*.mis"])))))
    (doto dcg-chooser
      (.setTitle "Choose DCG Executable")
      (.. getExtensionFilters
          (add
            (FileChooser$ExtensionFilter. "DCG Executable (il2dcg.exe)" (into-array ["il2dcg.exe"])))))))