;; 
;;
(ns uk.org.il2ssd.jfx.ui

  (:import (javafx.application Platform)
           (javafx.scene.control TextArea Button TextField TextInputControl ChoiceBox TableColumn$CellEditEvent
                                 TableColumn TablePosition Label TableView)
           (java.util List)
           (javafx.stage Stage FileChooser)
           (javafx.scene.layout BorderPane)
           (javafx.scene.input KeyEvent)
           (uk.org.il2ssd DifficultySetting CycleMission)
           (java.io File)
           (javafx.event EventHandler))

  (:require [uk.org.il2ssd.state :as state]
            [uk.org.il2ssd.jfx.util :as util]))

(defn exit [] (Platform/exit))

(defn clear-diff-data [^List diff-data]
  (.clear diff-data))

(defn add-diff-data [^List diff-data item]
  (.add diff-data item))

(defn print-console [^TextArea console text]
  (util/run-later (.appendText console text)))

(defn clear-input [^TextInputControl control]
  (.clear control))

(defn set-title [^Stage stage title]
  (util/run-later (.setTitle stage title)))

(defn set-ui-connected [connected]
  (let [{:keys [^Button connect-btn
                ^Button disconn-btn
                ^List diff-data
                ^Button load-btn
                ^Button get-diff-btn
                ^Button set-diff-btn
                ^TextField cmd-entry
                ^TextArea console]}
        @state/controls]
    (if connected
      (util/run-later (do (.setDisable connect-btn true)
                          (.setDisable disconn-btn false)
                          (.setDisable get-diff-btn false)
                          (.setDisable set-diff-btn false)
                          (.setDisable cmd-entry false)
                          (.clear console)))
      (util/run-later (do (.setDisable connect-btn false)
                          (.setDisable disconn-btn true)
                          (.setDisable load-btn true)
                          (.setDisable get-diff-btn true)
                          (.setDisable set-diff-btn true)
                          (.setDisable cmd-entry true)
                          (.setText console "<disconnected>")
                          (set-title))))))

(defn set-ui-playing [playing]
  (let [{:keys [^TableView diff-table
                ^Button set-diff-btn
                ^Button start-btn]} @state/controls]
    (if playing
      (util/run-later (do (.setEditable diff-table false)
                          (.setDisable set-diff-btn true)
                          (.setText start-btn "\uf04d Stop")))
      (util/run-later (do (.setEditable diff-table true)
                          (.setDisable set-diff-btn false)
                          (.setText start-btn "\uf04b Start"))))))

(defn set-ui-loaded [loaded]
  (let [{:keys [^Button start-btn
                ^Button load-btn]} @state/controls]
    (if loaded
      (util/run-later (do (.setDisable start-btn false)
                          (.setDisable load-btn false)
                          (.setText load-btn "\uf05e Unload")))
      (util/run-later (do (.setDisable start-btn true)
                          (.setText load-btn "\uf093 Load")
                          (if @state/mis-selected
                            (.setDisable load-btn false)
                            (.setDisable load-btn true)))))))

(defn get-text ^String [control]
  (.getText control))

(defn set-mis-pane [^BorderPane mis-pane content]
  (.setCenter mis-pane content))

(defn get-choice ^String [^ChoiceBox choicebox]
  (str (.getValue choicebox)))

(defn get-key ^String [^KeyEvent event]
  (-> event .getCode .getName))

(defn set-label [^Label label text]
  (.setText label text))

(defn show-chooser ^File [^FileChooser chooser]
  (.showOpenDialog chooser (Stage.)))

(defn difficulty-validator
  "### difficulty-validator
   This one argument function returns logical true if the value passed in for
   validation is equal to either the string value \"0\" or the string value \"1\",
   the two permissible values to which a difficulty setting can be set on the
   server console."
  [newval]
  (or (= newval "0")
      (= newval "1")))

(defn cycle-validator
  "### cycle-validator
   This one argument function returns logical true if the value passed in for
   validation can be coerced to an Integer value and is greater than zero, since
   we define the mission timers in whole minutes."
  [newval]
  (if
      (try (pos? (Integer/decode newval))
           (catch Exception e nil))
    true))

(defn diff-val-commit
  "### diff-val-commit
   This zero argument function returns an EventHandler which triggers an update of
   the list backing the difficulty TableView when the user commits their cell edit.

   In this case we commit the edit to the backing list if the new value passes the
   difficulty setting validator function.

   Otherwise, we toggle visibility of the column off and then on again to trigger
   a refresh of the data shown from the backing list, indicating to the user that
   their value was not committed."
  ^EventHandler []
  (util/event-handler
    [^TableColumn$CellEditEvent cell]
    (let [^TableColumn col (.getTableColumn cell)
          index (-> cell ^TablePosition .getTablePosition .getRow)
          ^List items (-> cell .getTableView .getItems)
          ^DifficultySetting current (.get items index)
          ^String newval (.getNewValue cell)]
      (if (difficulty-validator newval)
        (.setValue current newval)
        (doto col (.setVisible false)
                  (.setVisible true))))))

(defn cycle-timer-commit
  "### cycle-timer-commit
   This zero argument function returns an EventHandler which triggers an update of
   the list backing the mission cycle TableView when the user commits their cell
   edit.

   We commit the edit to the backing list if the new value passes the mission cycle
   validator function.

   Otherwise, we toggle visibility of the column off and then on again to trigger
   a refresh of the data shown from the backing list, indicating to the user that
   their value was not committed."
  ^EventHandler []
  (util/event-handler
    [^TableColumn$CellEditEvent cell]
    (let [^TableColumn col (.getTableColumn cell)
          index (-> cell ^TablePosition .getTablePosition .getRow)
          ^List items (-> cell .getTableView .getItems)
          ^CycleMission current (.get items index)
          ^String newval (.getNewValue cell)]
      (if (cycle-validator newval)
        (.setTimer current newval)
        (doto col (.setVisible false)
                  (.setVisible true))))))