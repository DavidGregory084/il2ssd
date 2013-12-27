;;;;
;;;; Channel definitions
;;;;
(ns uk.org.il2ssd.channel
    (:require [clojure.core.async :refer [chan filter< mult tap]]))

(def in-channel (chan))
(def cln-channel (filter< #(= (re-find #"<consoleN><\d+>" %) nil) in-channel))
(def mult-channel (mult cln-channel))
(def print-channel (let [c (chan)]
                       (tap mult-channel c)))
(def diff-channel (let [c (chan)]
                      (filter< #(re-find #"\w+\s+[0-1]\s+" %) (tap mult-channel c))))
(def mis-channel (let [c (chan)]
                     (filter< #(re-find #"Mission" %) (tap mult-channel c))))
