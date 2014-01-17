;; ## Channels for thread-safe concurrency
;;
;; Here we define the input channels that we use to safely process the server console
;; output concurrently. The initial input channel will receive one line at a time from
;; the server socket.
;;
;; We are using unbuffered channels. This means that once a value is placed onto the
;; channel, further puts onto the channel block until this value is taken from the
;; channel.
;;
;; To route console output to various different processes we define a "mult".
;; This is a channel which may be tapped by a number of other channels so that each
;; of these channels can receive every value that is put onto the mult.
;;
;; If we didn't do this, each value would only be available on a "first come, first
;; served" basis.
;;
;; Various "taps" are then defined. These are the channels which each receive a copy
;; of every input onto the mult.
;;
;; Because we are using unbuffered channels, further puts onto the mult will block
;; until every tap is empty.
(ns uk.org.il2ssd.channel
  (:require [clojure.core.async :refer [chan filter< mult tap]]))

(def in-channel
  "### in-channel
   This is the input channel from the Il-2 dedicated server console. It receives values
   one line at a time from the server socket."
  (chan))

(def mult-channel
  "### mult-channel
   This mult channel filters the input values from in-channel above. We filter the
   channel to accept only lines which don't begin with the server console prompt
   text, so that only genuine output from the console is used in later processing."
  (mult
    (filter<
      #(not (re-find #"<consoleN><\d+>" %))
      in-channel)))

(def print-channel
  "### print-channel
   This channel taps mult-channel, and is used to print directly to the server console
   text area control."
  (let [c (chan)]
    (tap mult-channel c)))

(def diff-channel
  "### diff-channel
   This channel taps mult-channel, and filters for lines which consist of a single
   word, followed by a single number which is either 0 or 1.

   This channel is used to parse difficulty settings from the server console output."
  (filter<
    #(re-find #"[A-Z[a-z]]+\s*[0-1]\s+" %)
    (tap mult-channel (chan))))

(def mis-channel
  "### mis-channel
   This channel taps mult-channel, and filters for lines which contain the string
   \"Mission\". This channel is used to parse mission load, begin, end and unload
   events from the server console output.

   The filtering used will need to be revised before chat parsing is enabled so
   that server chat which contains this common word does not end up on the channel."
  (let [c (chan)]
    (filter<
      #(re-find #"Mission" %)
      (tap mult-channel c))))
