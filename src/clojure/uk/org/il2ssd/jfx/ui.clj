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
                                 TextArea TextField TextInputControl TablePosition
                                 ToolBar TableRow)
           (javafx.scene.layout BorderPane)
           (javafx.stage FileChooser Stage)
           (uk.org.il2ssd.jfx CycleMission DifficultySetting Pilot Ban)))

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

(defn clear-bans-data
  [controls]
  (let [{:keys [^List bans-data]} controls]
    (util/run-later (.clear bans-data))))

(defn add-ban-data
  [^List bans-data
   ^String type
   ^String value]
  (util/run-later (.add bans-data (Ban. type value))))

(defn get-ban
  ([^List bans-data index]
   (let [^Ban ban (.get bans-data index)]
     (-> {}
         (assoc :type (.getType ban))
         (assoc :value (.getValue ban))))))

(defn clear-pilots-data
  [controls]
  (let [{:keys [^List pilots-data]} controls]
    (util/run-later (.clear pilots-data))))

(defn add-pilot-data
  ([^List pilots-data
    ^String socket
    ^String ip
    ^String name]
   (util/run-later (.add pilots-data (Pilot. socket ip name))))
  ([^List pilots-data
    ^String number
    ^String socket
    ^String ip
    ^String name]
   (util/run-later (.add pilots-data (Pilot. number socket ip name)))))

(defn update-pilot-data
  ([pilots-data number score team]
   (doseq [^Pilot pilot pilots-data
           :when (= (.getNumber pilot) number)]
     (util/run-later
       (doto pilot
         (.setScore (Long/decode score))
         (.setTeam team)))))
  ([pilots-data number socket ip name]
   (let [update-pilot (for [^Pilot pilot pilots-data
                            :when (= (.getSocket pilot) socket)]
                        (do (util/run-later (.setNumber pilot number))
                            true))]
     (when-not (some true? update-pilot)
       (add-pilot-data pilots-data number socket ip name)))))

(defn remove-pilot-data
  [^List pilots-data
   ^String socket]
  (doseq [^Pilot pilot pilots-data]
    (when (= (.getSocket pilot) socket)
      (util/run-later (.remove pilots-data pilot)))))

(defn get-pilot
  ([^List pilots-data index]
   (let [^Pilot pilot (.get pilots-data index)]
     (-> {}
         (assoc :number (.getNumber pilot))
         (assoc :name (.getName pilot))
         (assoc :ip (.getIp pilot))))))

(defn get-nth-in-string
  "### get-nth-in-string
   This function takes an integer n, a character ch and some input text, and
   returns the index of the nth occurrence of character ch in the text."
  [n ch in-text]
  (loop [index -1
         found 0
         ^String text in-text]
    (if (or (< n 1) (= found n))
      index
      (let [found-idx (.indexOf text (int ch))]
        (if (neg? found-idx)
          found-idx
          (recur (int (+ index (inc found-idx)))
                 (inc found)
                 (.substring text (inc found-idx))))))))

(defn print-console
  "### print-console
   This two argument function appends the supplied text to the supplied text area
   control. We also delete old lines when the line count reaches 1000 lines."
  [^TextArea console new-text]
  (let [console-text (.getText ^TextArea console)
        newlines (partial filter #(= % \newline))
        current-count (count (newlines console-text))
        new-count (count (newlines new-text))
        overflow (- (+ current-count new-count) 1000)
        delete-index (get-nth-in-string overflow \newline console-text)]
    (when (> delete-index -1)
      (util/run-now (.deleteText console 0 delete-index))))
  (util/run-now (.appendText console new-text)))

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
  [show controls]
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

(defn toggle-dcg-start-txt
  "### toggle-dcg-start-txt
   This two argument function sets the controls in the supplied map of controls to
   the correct state for the supplied DCG running state."
  [running controls]
  (let [{:keys [^Button dcg-start-btn]} controls]
    (if running
      (util/run-later (.setText dcg-start-btn "\uf04d \uf021 Stop"))
      (util/run-later (.setText dcg-start-btn "\uf04b \uf021 Start")))))

(defn swap-to-button
  "### swap-to-button
   This three argument function removes the listed buttons and adds the button
   requested at the leftmost index out of all the original buttons provided.
   This function is idempotent; that is, it can be run multiple times for the
   same inputs and regardless of the original state the result will be the same."
  [^ToolBar toolbar to-add to-remove]
  (let [items (.getItems toolbar)
        remove-indices (sort > (map #(.indexOf items %) to-remove))]
    (loop [indices remove-indices]
      (when-let [index (first indices)]
        (when (> index -1)
          (.remove items (int index)))
        (recur (next indices))))
    (let [add-index (.indexOf items to-add)
          all-indices (conj remove-indices add-index)
          target (apply min (filter #(> % -1) all-indices))]
      (when (and (> add-index -1) (not= add-index target))
        (.remove items (int add-index))
        (.add items target to-add))
      (when (neg? add-index)
        (.add items target to-add)))))

(defn set-button-state
  "### set-button-state
   This two argument function takes a global state map and the controls map and
   enables each control for which every one of the enablement dependencies is
   satisfied and none of the disablement dependencies are met. Any controls
   which do not match these conditions are disabled.
   Controls which do not specify either enablement or disablement dependencies
   are ignored."
  [state controls]
  (doseq [control (vals controls)
          :let [{:keys [enabled-by disabled-by]
                 :or   [:enabled-by #{}
                        :disabled-by #{}]} control]
          :when (or (contains? control :enabled-by)
                    (contains? control :disabled-by))]
    (if (and (or (empty? enabled-by)
                 (every? state enabled-by))
             (or (empty? disabled-by)
                 (not-any? state disabled-by)))
      (util/run-later (.setDisable ^Node (:instance control) false))
      (util/run-later (.setDisable ^Node (:instance control) true)))))

(defn highlight-table-row
  "### highlight-table-row
   This function highlights the row at the specified index in the mission
   cycle table. First the highlightRow style class is removed from all rows.
   Next, the index of the table row is compared to the current cycle index.
   If they are the same the highlightRow style class is added.
   Passing a nonexistent index (e.g. -1) simply removes highlighting for all
   rows."
  [index controls]
  (let [{:keys [^TableView cycle-table]} controls
        rows (.lookupAll cycle-table "TableRow")]
    (doseq [^TableRow row rows
            :let [row-index (.getIndex row)]]
      (-> row
          .getStyleClass
          (.remove "highlightRed"))
      (when (= row-index index)
        (-> row
            .getStyleClass
            (.add "highlightRed"))))))

(defn highlight-team
  [number team controls]
  (let [{:keys [^TableView pilots-table]} controls
        rows (.lookupAll pilots-table "TableRow")]
    (doseq [^TableRow row rows
            :let [row-number (try
                               (-> row .getItem .getNumber)
                               (catch NullPointerException _))]
            :when (= row-number number)]
      (when (= team "Blue")
        (-> row
            .getStyleClass
            (.remove "highlightRed"))
        (-> row
            .getStyleClass
            (.add "highlightBlue")))
      (when (= team "Red")
        (-> row
            .getStyleClass
            (.remove "highlightBlue"))
        (-> row
            .getStyleClass
            (.add "highlightRed")))
      (when (= team "None")
        (-> row
            .getStyleClass
            (.removeAll
              (into-array String ["highlightRed" "highlightBlue"])))))))