;;;;
;;;; Channel definitions
;;;;
(ns uk.org.il2ssd.channel
  (:require [clojure.core.async :refer [chan filter< mult tap]]))

(def in-channel
  "Input channel from the Il-2 dedicated server console. Receives values from server/socket-listener."
  (chan))

(def mult-channel
  "Filters console input prompt text from the server console input channel and returns a mult."
  (mult
    (filter<
      #(not (re-find #"<consoleN><\d+>" %))
      in-channel)))

(def print-channel
  "Taps mult-channel to print directly to the console text area."
  (let [c (chan)]
    (tap mult-channel c)))

(def diff-channel
  "Filters mult-channel for difficulty settings."
  (let [c (chan)]
    (filter<
      #(re-find #"\w+\s+[0-1]\s+" %)
      (tap mult-channel c))))

(def mis-channel
  "Filters mult-channel for mission status lines"
  (let [c (chan)]
    (filter<
      #(re-find #"Mission" %)
      (tap mult-channel c))))
