;;;;
;;;; Channel definitions
;;;;
(ns uk.org.il2ssd.channel
    (:require [clojure.core.async :refer [chan filter< mult tap]]))

(def in-channel (chan))
(def cln-channel (filter< #(= (re-find #"<consoleN><\d+>" %) nil) in-channel))
(def mult-channel (mult cln-channel))
(def parse-channel (let [c (chan)]
                       (filter< #(not= (re-find #"\s*\w+\s+[0-1]\s+" %) nil) (tap mult-channel c))))
(def print-channel (let [c (chan)]
                       (tap mult-channel c)))

(def ui-channel (let [c (chan)]
                    c))