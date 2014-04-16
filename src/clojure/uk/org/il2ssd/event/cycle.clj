;;
;; ## Mission cycle scheduler
;;
;; This namespace contains functions to schedule mission cycle load events
;; using the at-at scheduling library.
(ns uk.org.il2ssd.event.cycle
  (:require [clojure.string :as string]
            [uk.org.il2ssd.event.mission :as mission]
            [uk.org.il2ssd.server :as server]
            [uk.org.il2ssd.state :as state]
            [uk.org.il2ssd.jfx.ui :as ui]
            [overtone.at-at :refer [after]])
  (:import (java.io File)
           (uk.org.il2ssd.jfx CycleMission)))

(defn mission-swap
  [f]
  (let [{:keys [cycle-table
                cycle-data]} @state/controls
        list-size (dec (ui/get-list-size cycle-data))
        source (ui/get-selected-index cycle-table)
        target (f source)]
    (when (and (>= target 0)
               (<= target list-size)
               (not= source target))
      (ui/swap-list-items cycle-data source target)
      (ui/select-table-index cycle-table target))))

(defn mission-delete
  []
  (let [{:keys [cycle-table
                cycle-data]} @state/controls
        index (ui/get-selected-index cycle-table)]
    (when (>= index 0)
        (ui/remove-list-item cycle-data index))))

(defn mission-add
  []
  (let [{:keys [cycle-data
                cycle-path-fld]} @state/controls
        mission (ui/get-text cycle-path-fld)]
    (when (not (string/blank? mission))
      (ui/clear-input cycle-path-fld)
      (ui/add-cycle-data cycle-data mission "60"))))

(defn cycle-choose-command
  "### cycle-choose-command
   This zero argument function gets the mission file selected by the user
   after displaying a mission file chooser. This value is relativised
   against the Missions directory so that it is in the format expected
   by the server in LOAD commands.

   This value is loaded into the cycle mission path field as the mission
   to add to the cycle."
  []
  (let [{:keys [mis-chooser
                cycle-data]} @state/controls
        file (ui/show-chooser mis-chooser)]
    (when file
      (ui/add-cycle-data
        cycle-data
        (mission/get-relative-path @state/server-path (.getCanonicalPath file))
        "60"))))