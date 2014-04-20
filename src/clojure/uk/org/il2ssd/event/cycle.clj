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
            [overtone.at-at :refer [after mk-pool
                                    scheduled-jobs
                                    show-schedule
                                    stop
                                    stop-and-reset-pool!]])
  (:import (java.io File)
           (uk.org.il2ssd.jfx CycleMission)
           (overtone.at_at ScheduledJob)))

(declare load-cycle-mis)

(def cycle-schedule
  (mk-pool))

(def scheduled-mis
  (atom nil))

(defn mins-to-ms
  [mins]
  (* (Integer/decode mins) 60000))

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
      (ui/add-cycle-data cycle-data mission "60")
      (reset! state/mission-path
              (:mission (ui/get-cycle-mission cycle-data @state/cycle-index))))))

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

(defn set-cycle-running
  [_ _ _ running]
  (ui/set-ui-cycle running @state/controls))

(defn next-mission
  [scheduled]
  (when (not scheduled)
    (stop @scheduled-mis))
  (let [{:keys [cycle-data]} @state/controls
        last-mission (dec (ui/get-list-size cycle-data))]
    (if (= @state/cycle-index last-mission)
      (reset! state/cycle-index 0)
      (swap! state/cycle-index inc))
    (load-cycle-mis)))

(defn start-cycle
  []
  (reset! state/cycle-running true)
  (load-cycle-mis))

(defn load-cycle-mis
  []
  (let [{:keys [cycle-data]} @state/controls
        mission-data (ui/get-cycle-mission cycle-data @state/cycle-index)
        mission (:mission mission-data)
        timer (mins-to-ms (:timer mission-data))]
    (ui/toggle-prog-ind @state/controls true)
    (server/load-begin-mission mission)
    (->> (after timer #(next-mission true) cycle-schedule)
         (reset! scheduled-mis))))

(defn stop-cycle
  []
  (ui/toggle-prog-ind @state/controls true)
  (stop-and-reset-pool! cycle-schedule :strategy :kill)
  (ui/toggle-prog-ind @state/controls false)
  (reset! scheduled-mis nil)
  (reset! state/cycle-running false)
  (reset! state/cycle-index 0))