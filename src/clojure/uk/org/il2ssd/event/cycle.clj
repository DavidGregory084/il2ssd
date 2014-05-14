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
                                    kill
                                    scheduled-jobs
                                    show-schedule
                                    stop
                                    stop-and-reset-pool!]])
  (:import (java.io File)
           (uk.org.il2ssd.jfx CycleMission)))

(declare load-cycle-mis)

(def cycle-schedule
  "### cycle-schedule
   This is the scheduled task pool for the cycle mission scheduler."
  (mk-pool))

(def scheduled-mis
  "### scheduled-mis
   This is the next scheduled mission load event. It's stored in an atom
   so that it can be cancelled if the user loads the next mission explicitly."
  (atom nil))

(defn mins-to-ms
  "### mins-to-ms
   This function converts a minutes string to a millisecond integer value."
  [mins]
  (* (Integer/decode mins) 60000))

(defn mission-swap
  "### mission-swap
   This one argument applies the provided function to the current selected
   mission index, swapping the selected mission with the one stored at the
   target index, provided the target index is valid."
  [f]
  (let [{:keys [cycle-table
                cycle-data]} @state/control-instances
        list-size (dec (ui/get-list-size cycle-data))
        source (ui/get-selected-index cycle-table)
        target (f source)]
    (when (and (>= target 0)
               (<= target list-size)
               (not= source target))
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
        (when (= size 0)
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
    (when (not (string/blank? mission))
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
  (when (not scheduled)
    (stop @scheduled-mis))
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
        timer (mins-to-ms (:timer mission-data))]
    (reset! state/loading true)
    (server/load-begin-mission mission)
    (->> (after timer #(next-mission true) cycle-schedule)
         (reset! scheduled-mis))))

(defn stop-cycle
  "### stop-cycle
   This function stops the cycle by resetting the schedule pool and
   each of the state atoms associated with the cycle scheduler."
  []
  (stop @scheduled-mis)
  (stop-and-reset-pool! cycle-schedule :strategy :kill)
  (reset! scheduled-mis nil)
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