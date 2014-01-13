;; ## JavaFX TableView and underlying data model functions
;;
;; In this namespace we define the data models that we will use for the lists that
;; back the TableViews and initialise the TableViews with the properties that we
;; require.
;;
;; In this case we will define the tables' values to be backed by a JavaFX
;; ObservableList comprised of Java beans that we define in Java.
(ns uk.org.il2ssd.ui-tables
  (:import (javafx.collections ObservableList FXCollections)
           (javafx.scene.control.cell PropertyValueFactory TextFieldTableCell)
           (javafx.scene.control TableView TableColumn TableColumn$CellEditEvent TablePosition)
           (javafx.event EventHandler)
           (uk.org.il2ssd CycleMission DifficultySetting))
  (:require [uk.org.il2ssd.jfx :as jfx]
            [uk.org.il2ssd.state :as state]))

(defn init-diff-table
  "### init-diff-table
   This is a zero argument function which instantiates the cell factories and cell
   value factories for the difficulty table so that the table is populated
   correctly. The property which backs each column is defined in the constructor
   for the PropertyValueFactory for that column.

   We also define the CellFactory for the difficulty value column as
   TextFieldTableCell, which produces editable table cells.

   The backing list for the table is instantiated and stored in the controls
   atom before being linked to the table.

   Finally, we define an EventHandler for the commit action which rejects any
   inputs which are not equal to 0 or 1 as these are the permitted values for
   Il-2 difficulty settings."
  []
  (let [{:keys [^TableView diff-table
                ^TableColumn diff-set-col
                ^TableColumn diff-val-col]} @state/controls]
    (.setCellValueFactory diff-set-col (PropertyValueFactory. "setting"))
    (.setCellValueFactory diff-val-col (PropertyValueFactory. "value"))
    (.setCellFactory diff-val-col (TextFieldTableCell/forTableColumn))
    (.setColumnResizePolicy diff-table TableView/CONSTRAINED_RESIZE_POLICY)
    (swap! state/controls assoc :diff-data (FXCollections/observableArrayList))
    (let [{:keys [^ObservableList diff-data]} @state/controls]
      (.setItems diff-table diff-data)
      (.setOnEditCommit diff-val-col
                        (jfx/event-handler
                          [^TableColumn$CellEditEvent cell]
                          (let [^TableColumn col (.getTableColumn cell)
                                index (-> cell .getTablePosition .getRow)
                                ^ObservableList items (-> cell .getTableView .getItems)
                                ^DifficultySetting current (.get items index)
                                ^String setting (.setting current)
                                ^String newval (.getNewValue cell)]
                            (if (or (= newval "0")
                                    (= newval "1"))
                              (.setValue current newval)
                              (doto col (.setVisible false)
                                        (.setVisible true)))))))))

(defn init-cycle-table
  "### init-diff-table
 This is a zero argument function which instantiates the cell factories and cell
 value factories for the mission cycle table so that the table is populated
 correctly. The property which backs each column is defined in the constructor
 for the PropertyValueFactory for that column.

 We also define the CellFactory for the mission timer column as
 TextFieldTableCell, which produces editable table cells.

 The backing list for the table is instantiated and stored in the controls atom
 before being linked to the table.

 Finally, we define an EventHandler for the commit action which rejects inputs
 which cannot be converted to an Integer or which are not greater than zero."
  []
  (let [{:keys [^TableView cycle-table
                ^TableColumn cycle-mis-col
                ^TableColumn cycle-tim-col]} @state/controls]
    (.setCellValueFactory cycle-mis-col (PropertyValueFactory. "mission"))
    (.setCellValueFactory cycle-tim-col (PropertyValueFactory. "timer"))
    (.setCellFactory cycle-tim-col (TextFieldTableCell/forTableColumn))
    (.setColumnResizePolicy cycle-table TableView/CONSTRAINED_RESIZE_POLICY)
    (swap! state/controls assoc :cycle-data (FXCollections/observableArrayList))
    (let [{:keys [^ObservableList cycle-data]} @state/controls]
      (.setItems cycle-table cycle-data)
      (.setOnEditCommit cycle-tim-col
                        (jfx/event-handler
                          [^TableColumn$CellEditEvent cell]
                          (let [^TableColumn col (.getTableColumn cell)
                                index (-> cell .getTablePosition .getRow)
                                ^ObservableList items (-> cell .getTableView .getItems)
                                ^CycleMission current (.get items index)
                                ^String setting (.timer current)
                                ^String newval (.getNewValue cell)]
                            (if
                                (try
                                  (> (Integer/decode newval) 0)
                                  (catch NumberFormatException e nil)
                                  (catch NullPointerException e nil))
                              (.setTimer current newval)
                              (doto col (.setVisible false)
                                        (.setVisible true)))))))))