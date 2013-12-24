;;;;
;;;; Channels
;;;;
(ns uk.org.il2ssd.channel
    (:require [clojure.core.async :refer [chan filter<]]))

(def in-channel (chan))
(def cln-channel (filter< #(= (re-find #"<consoleN><\d+>" %) nil) in-channel))



