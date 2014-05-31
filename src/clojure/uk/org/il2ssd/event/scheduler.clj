(ns uk.org.il2ssd.event.scheduler
  (:require [overtone.at-at :refer [after
                                    mk-pool
                                    kill
                                    stop
                                    stop-and-reset-pool!]]))

(def schedule
  "### cycle-schedule
   This is the scheduled task pool for the cycle mission scheduler."
  (mk-pool))

(def scheduled-mis
  "### scheduled-mis
   This is the next scheduled mission load event. It's stored in an atom
   so that it can be cancelled if the user loads the next mission explicitly."
  (atom nil))

(defn reset-schedule
  []
  (stop-and-reset-pool! schedule :strategy :kill)
  (reset! scheduled-mis nil))

(defn mins-to-ms
  "### mins-to-ms
   This function converts a minutes string to a millisecond integer value."
  [mins]
  (* (try (Integer/decode mins)
          (catch NumberFormatException _ 60))
     60000))

(defn stop-scheduled-mis
  []
  (stop @scheduled-mis))

(defn schedule-mission
  [f timer]
  (reset! scheduled-mis (after (mins-to-ms timer) f schedule)))