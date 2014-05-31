;;
;; ## Mission cycle scheduler
;;
;; This namespace contains functions to schedule mission cycle load events
;; using the at-at scheduling library.
(ns uk.org.il2ssd.event.cycle
  (:require [clojure.string :as string]
            [uk.org.il2ssd.event.mission :as mission]
            [uk.org.il2ssd.event.scheduler :as schedule]
            [uk.org.il2ssd.server :as server]
            [uk.org.il2ssd.state :as state]
            [uk.org.il2ssd.jfx.ui :as ui])
  (:import (java.io File)
           (uk.org.il2ssd.jfx CycleMission)))

(declare load-cycle-mis)

(defn mission-swap
  "### mission-swap
   This one argument applies the provided function to the current selected
   mission index, swapping the selected mission with the one stored at the
   target index, provided the target index is valid."
  [f]
  (let [{:keys [cycle-table
                cycle-data]} @state/control-instances
        max (dec (ui/get-list-size cycle-data))
        source (ui/get-selected-index cycle-table)
        new (f source)
        target (cond
                 (neg? new) 0
                 (> new max) max
                 :else new)]
    (when (not= source target)
      (ui/swap-list-items cycle-data source target)
      (ui/select-table-index cycle-table target))))

(defn mission-delete
  "### mission-delete
   This function deletes the mission at the current selected index, providing
   the current selected index is valid."
  []
  (let [{:keys [cycle-table
                cycle-data]} @state/control-instances
        index (ui/get-selected-index cycle-table)]
    (when (>= index 0)
      (ui/remove-list-item cycle-data index)
      (let [size (ui/get-list-size cycle-data)]
        (when (zero? size)
          (reset! state/cycle-mission-path nil))))))

(defn mission-add
  "### mission-add
   This function adds the mission path in the cycle mission path field to the
   list, with an initial value of 60 minutes as the timer. It also sets the
   cycle mission atom to the cycle mission stored at the current value of the
   cycle index."
  []
  (let [{:keys [cycle-data
                cycle-path-fld]} @state/control-instances
        mission (ui/get-text cycle-path-fld)]
    (when-not (string/blank? mission)
      (ui/clear-input cycle-path-fld)
      (ui/add-cycle-data cycle-data mission "60")
      (reset! state/cycle-mission-path
              (:mission (ui/get-cycle-mission cycle-data @state/cycle-index))))))

(defn cycle-choose-command
  "### cycle-choose-command
   This zero argument function gets the mission file selected by the user
   after displaying a mission file chooser. This value is relativised
   against the Missions directory so that it is in the format expected
   by the server in LOAD commands.

   This value is loaded into the cycle mission list, and the cycle mission
   atom is reset to the mission path at the current cycle index."
  []
  (let [{:keys [mis-chooser
                cycle-data]} @state/control-instances
        file (ui/show-chooser mis-chooser)]
    (when file
      (ui/add-cycle-data
        cycle-data
        (mission/get-relative-path @state/server-path (.getCanonicalPath file))
        "60")
      (reset! state/cycle-mission-path
              (:mission (ui/get-cycle-mission cycle-data @state/cycle-index))))))

(defn next-mission
  "### next-mission
   This one argument function increments the index and loads the mission at
   that index, unless the initial index was the last index, in which case the
   index is reset to 0 before loading.
   If this was not a scheduled load event, the active scheduled load event
   is cancelled - the user has chosen to override the scheduler by pressing
   the next button, or loading the mission at the previous index has failed."
  [scheduled]
  (when-not scheduled
    (schedule/stop-scheduled-mis))
  (let [{:keys [cycle-data]} @state/control-instances
        last-mission (dec (ui/get-list-size cycle-data))]
    (if (= @state/cycle-index last-mission)
      (reset! state/cycle-index 0)
      (swap! state/cycle-index inc))
    (load-cycle-mis)))

(defn start-cycle
  "### start-cycle
   This function resets the cycle-running atom and loads the first
   cycle mission. The cycle index is set to 0 when the program is
   initialised or when stopping the cycle."
  []
  (reset! state/cycle-running true)
  (reset! state/cycle-index 0)
  (load-cycle-mis))

(defn load-cycle-mis
  "### load-cycle-mis
   This function gets the cycle mission object from the list at the
   current cycle index and loads the mission. It schedules the
   next-mission function to trigger after the timer specified for
   the current mission and stores this scheduled event in an atom
   so that it may be cancelled later."
  []
  (let [{:keys [cycle-data]} @state/control-instances
        mission-data (ui/get-cycle-mission cycle-data @state/cycle-index)
        mission (:mission mission-data)
        timer (:timer mission-data)]
    (reset! state/loading true)
    (server/load-begin-mission mission)
    (schedule/schedule-mission #(next-mission true) timer)))

(defn stop-cycle
  "### stop-cycle
   This function stops the cycle by resetting the schedule pool and
   each of the state atoms associated with the cycle scheduler."
  []
  (schedule/stop-scheduled-mis)
  (schedule/reset-schedule)
  (reset! state/cycle-running false))

(defn start-stop-cycle-command
  "### start-stop-cycle-command
   This function stops the cycle if it is running, or starts it if it is not.
   In either case it stops any mission that is playing."
  []
  (when @state/playing
    (server/unload-mission))
  (if @state/cycle-running
    (stop-cycle)
    (start-cycle)))