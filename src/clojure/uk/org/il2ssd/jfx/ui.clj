;;
;; ## UI manipulation functions
;;
;; In this namespace we define all the functions which will be used to update
;; the UI. The objective is to keep all of the JavaFX-specific logic (other
;; than initialisation) within this namespace, so that the application logic
;; is independent of the UI framework.
;;
;; This namespace wraps UI update functions that will be called from other
;; threads in util/run-later calls so that all handling of the JavaFX
;; Application Thread is kept within this namespace and the main application
;; logic doesn't need to know about it.
(ns uk.org.il2ssd.jfx.ui
  (:require [uk.org.il2ssd.jfx.util :as util])
  (:import (java.io File)
           (java.nio.file Path Paths)
           (java.util List Collections)
           (javafx.application Platform)
           (javafx.event EventHandler)
           (javafx.scene Node)
           (javafx.scene.control Button ChoiceBox Label Labeled
                                 ProgressIndicator SelectionModel TableColumn
                                 TableColumn$CellEditEvent TableView
                                 TextArea TextField TextInputControl TablePosition ToolBar)
           (javafx.scene.layout BorderPane)
           (javafx.stage FileChooser Stage)
           (uk.org.il2ssd.jfx CycleMission DifficultySetting)))

(defn exit
  "### exit
   This zero argument function calls the static JavaFX Platform.exit method to
   close the application."
  [] (Platform/exit))

(defn clear-diff-data
  "### clear-diff-data
   This one argument function clears the list which it receives as an argument."
  [controls]
  (let [{:keys [^List diff-data]} controls]
    (util/run-later (.clear diff-data))))

(defn add-diff-data
  "### add-diff-data
   This two argument function adds the supplied element to the supplied list
   object."
  [^List diff-data
   ^String setting
   ^String value]
  (util/run-later (.add diff-data (DifficultySetting. setting value))))

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

(defn set-visible
  "### set-visible
   This two argument function toggles display of the provided control depending
   upon a boolean parameter."
  [^Node control show]
  (if show
    (util/run-later (.setVisible control true))
    (util/run-later (.setVisible control false))))

(defn toggle-prog-ind
  "### toggle-prog-ind
   This two argument function toggles display for the ProgressIndicator instance
   in the supplied controls map according to the show parameter."
  [controls show]
  (let [{:keys [^ProgressIndicator prog-ind]} controls]
    (if show
      (util/run-later (.setVisible prog-ind true))
      (util/run-later (.setVisible prog-ind false)))))

(defn toggle-console-text
  "### tpggle-console-text
   This two argument function sets the controls in the supplied map of controls
   to the relevant state for the connection state provided."
  [connected controls]
  (let [{:keys [^TextArea console]} controls]
    (if connected
      (util/run-later (.clear console))
      (util/run-later (.setText console "<disconnected>")))))

(defn toggle-start-txt
  "### toggle-start-txt
   This two argument function sets the controls in the supplied map of controls to
   the correct state for the supplied mission running state."
  [playing controls]
  (let [{:keys [^TableView diff-table
                ^Button start-btn]} controls]
    (if playing
      (util/run-later (do (.setEditable diff-table false)
                          (.setText start-btn "\uf04d Stop")))
      (util/run-later (do (.setEditable diff-table true)
                          (.setText start-btn "\uf04b Start"))))))

(defn toggle-load-txt
  "### toggle-load-txt
   This two argument function sets the controls in the supplied map of controls to
   the correct state for the supplied mission loaded state."
  [loaded controls]
  (let [{:keys [^Button load-btn]} controls]
    (if loaded
      (util/run-later (.setText load-btn "\uf05e Unload"))
      (util/run-later (.setText load-btn "\uf093 Load")))))

(defprotocol GetText
  "### GetText
   This protocol is simply used to eliminate calls to the Reflection API when
   calling the .getText method. This method is part of two very different
   interfaces in JavaFX, so each has been extended for type-based dispatch
   here."
  (get-text [control]))


(extend-protocol GetText

  TextInputControl
  (get-text [control]
    (.getText control))

  Labeled
  (get-text [control]
    (.getText control)))

(defn set-text
  "### set-text
   This two argument function sets the text in the supplied text control using
   the text provided."
  [^TextInputControl control text]
  (.setText control text))

(defn get-difficulty-setting
  "### get-item-data
   This one argument function returns a map containing the setting and value
   for the supplied DifficultySetting instance."
  [^DifficultySetting data-item]
  (-> {}
      (assoc :setting (.getSetting data-item))
      (assoc :value (.getValue data-item))))

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
   This two argument function sets the text content of the supplied Label to the
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
           (catch Exception _ nil))
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


(defn set-mis-dir
  "### set-mis-dir
   This two argument function sets the initial directory for the mission
   chooser to the Missions directory of the server whose .exe is selected."
  [path controls]
  (let [{:keys [^FileChooser mis-chooser]} controls]
    (util/run-later
      (.setInitialDirectory mis-chooser
                            (-> (Paths/get path (into-array String []))
                                .getParent
                                (.resolve "Missions")
                                .toFile)))))

(defn get-list-size
  "### get-list-size
   This one argument function returns the size of the provided list."
  [^List list]
  (.size list))

(defn get-selected-index
  "### get-selected-index
   This one argument function returns the index of the currently selected
   item."
  [^TableView table]
  (-> table
      .getSelectionModel
      .getSelectedIndex))

(defn get-index-of-mission
  "### get-index-of-mission
   This function returns the index of the supplied CycleMission object in
   the cycle mission list."
  [^List cycle-data ^CycleMission mission]
  (.indexOf cycle-data mission))

(defn get-cycle-mission
  "### get-cycle-mission
   This two argument function retrieves the CycleMission object at the
   specified index in the cycle mission list.
   If an index is not specified, it is assumed that the instance itself
   has been provided."
  ([^CycleMission mission]
   (-> {}
       (assoc :mission (.getMission mission))
       (assoc :timer (.getTimer mission))))
  ([^List cycle-data index]
   (let [^CycleMission mission (.get cycle-data index)]
     (-> {}
         (assoc :mission (.getMission mission))
         (assoc :timer (.getTimer mission))))))

(defn swap-list-items
  "### swap-list-items
   This three argument function swaps the items in the list at the given
   indices."
  [list item1 item2]
  (Collections/swap list item1 item2))

(defn remove-list-item
  "### remove-list-item
   This two argument function removes the item at the specified index from
   the supplied list."
  [^List list ^Integer index]
  (.remove list (int index)))

(defn add-cycle-data
  "### add-cycle-data
   This three argument function instantiates a Cyclemission and adds it to
   the supplied list object."
  [^List cycle-data
   ^String mission
   ^String timer]
  (.add cycle-data (CycleMission. mission timer)))

(defn select-table-index
  "### select-table-index
   This two argument function selects the item at the given index in the
   table."
  [^TableView table index]
  (-> table
      .getSelectionModel
      (.clearAndSelect index)))

(defn toggle-cycle-start-txt
  "### toggle-cycle-start-txt
   This two argument function sets the controls in the supplied map of controls to
   the correct state for the supplied mission cycle running state."
  [running controls]
  (let [{:keys [^Button cycle-start-btn]} controls]
    (if running
      (util/run-later (.setText cycle-start-btn "\uf04d \uf021 Stop"))
      (util/run-later (.setText cycle-start-btn "\uf04b \uf021 Start")))))

(defn swap-start-button
  "### swap-start-button
   This three argument function swaps the given buttons in the supplied toolbar."
  [^ToolBar toolbar oldbutton newbutton]
  (let [items (.getItems toolbar)
        oldindex (.indexOf items oldbutton)]
    (when (> oldindex 0)
      (.remove items (int oldindex)))
    (let [newindex (.indexOf items newbutton)
          target (apply min (filter #(> % 0) (list oldindex newindex)))]
      (when (and (> newindex 0) (not= newindex target))
        (.remove items (int newindex))
        (.add items target newbutton))
      (when (< newindex 0)
        (.add items target newbutton)))))

(defn set-button-state
  "### set-button-state
   This two argument function takes a global state map and the controls map and
   enables each control for which every one of the enablement dependencies is
   satisfied and none of the disablement dependencies are met. Any controls
   which do not match these conditions are disabled.
   Controls which do not specify either enablement or disablement dependencies
   are ignored."
  [state controls]
  (let [map-keys (keys controls)]
    (doseq [key map-keys
            :let [control (key controls)
                  {:keys [enabled-by disabled-by]
                   :or   [:enabled-by #{}
                          :disabled-by #{}]} control]
            :when (or (contains? control :enabled-by)
                      (contains? control :disabled-by))]
      (if (and (or (empty? enabled-by)
                   (every? state enabled-by))
               (or (empty? disabled-by)
                   (not-any? state disabled-by)))
        (util/run-later (.setDisable ^Node (:instance control) false))
        (util/run-later (.setDisable ^Node (:instance control) true))))))