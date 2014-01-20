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

(defn exit
  "### exit
   This zero argument function calls the static JavaFX Platform.exit method to
   close the application."
  [] (Platform/exit))

(defn clear-diff-data
  "### clear-diff-data
   This one argument function clears the list which it receives as an argument."
  [^List diff-data]
  (util/run-later (.clear diff-data)))

(defn add-diff-data
  "### add-diff-data
   This two argument function adds the supplied element to the supplied list
   object."
  [^List diff-data item]
  (util/run-later (.add diff-data item)))

(defn print-console
  "### print-console
   This two argument function appends the supplied text to the supplied text area
   control."
  [^TextArea console text]
  (util/run-later (.appendText console text)))

(defn clear-input
  "### clear-input
   This one argument function clears the content of the supplied text control."
  [^TextInputControl control]
  (.clear control))

(defn set-title
  "### set-title
   This one argument function sets the stage title for the supplied stage to the
   supplied text value."
  [^Stage stage title]
  (util/run-later (.setTitle stage title)))

(defn set-ui-connected
  "### set-ui-connected
   This two argument function sets the controls in the supplied map of controls
   to the relevant state for the connection state provided."
  [connected controls]
  (let [{:keys [^Button connect-btn
                ^Button disconn-btn
                ^List diff-data
                ^Button load-btn
                ^Button get-diff-btn
                ^Button set-diff-btn
                ^TextField cmd-entry
                ^TextArea console]} controls]
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
                          (.setText console "<disconnected>"))))))

(defn set-ui-playing
  "### set-ui-playing
   This two argument function sets the controls in the supplied map of controls to
   the correct state for the supplied mission running state."
  [playing controls]
  (let [{:keys [^TableView diff-table
                ^Button set-diff-btn
                ^Button start-btn]} controls]
    (if playing
      (util/run-later (do (.setEditable diff-table false)
                          (.setDisable set-diff-btn true)
                          (.setText start-btn "\uf04d Stop")))
      (util/run-later (do (.setEditable diff-table true)
                          (.setDisable set-diff-btn false)
                          (.setText start-btn "\uf04b Start"))))))

(defn set-ui-loaded
  "### set-ui-loaded
   This two argument function sets the controls in the supplied map of controls to
   the correct state for the supplied mission running state."
  [loaded controls]
  (let [{:keys [^Button start-btn
                ^Button load-btn]} controls]
    (if loaded
      (util/run-later (do (.setDisable start-btn false)
                          (.setDisable load-btn false)
                          (.setText load-btn "\uf05e Unload")))
      (util/run-later (do (.setDisable start-btn true)
                          (.setText load-btn "\uf093 Load")
                          (if @state/mis-selected
                            (.setDisable load-btn false)
                            (.setDisable load-btn true)))))))

(defn get-text
  "### get-text
   This one argument function returns the text from the supplied control."
  ^String [control]
  (.getText control))

(defn set-mis-pane
  "### set-mis-pane
   This two argument function sets the centre content of the supplied BorderPane
   instance to the supplied object."
  [^BorderPane mis-pane content]
  (.setCenter mis-pane content))

(defn get-choice
  "### get-choice
   This one argument function gets the string value of the selected item in the
   supplied ChoiceBox instance."
  ^String [^ChoiceBox choicebox]
  (str (.getValue choicebox)))

(defn set-label
  "### set-label
   This two argument function sets the text content of the supplied control to the
   value of the supplied text argument."
  [^Label label text]
  (.setText label text))

(defn show-chooser
  "### show-chooser
   This one argument function shows the supplied file chooser dialog to the user
   and returns the selected file to the calling function."
  ^File [^FileChooser chooser]
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

(defn set-ui-server
  [path controls]
  (let [{:keys [single-path-btn cycle-path-btn]} controls]
    (if path
      (do (.setDisable single-path-btn false)
          (.setDisable cycle-path-btn false))
      (do (.setDisable single-path-btn true)
          (.setDisable cycle-path-btn true)))))