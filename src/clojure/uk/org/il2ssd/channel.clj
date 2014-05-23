;; ## Channels for thread-safe concurrency
;;
;; Here we define the input channels that we use to safely process the server console
;; output concurrently. The initial input channel will receive one line at a time from
;; the server socket.
;;
;; We are using channels with a buffer of ten values. This means that once ten values
;; are placed on a channel, further puts onto that channel block until a value is
;; taken from the channel.
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
;; Each put onto the mult must be taken by every tap before the next put can be taken.
;; This means that slow or unresponsive tapping processes can slow or even halt the
;; program.
;;
;; Because we are using buffered channels, up to ten items can be put onto the mult
;; before further puts will block until every channel has taken the oldest put.
(ns uk.org.il2ssd.channel
  (:require [clojure.core.async :refer [close! chan filter< mult remove<
                                        tap untap-all pipe map>]]))

(defn filter-pipe
  ([from to type & regexes]
   (pipe
     (filter<
       (fn [x] (some #(re-matches % x) regexes))
       (tap from (chan 10)))
     (map>
       #(assoc {} :type type :value %)
       to))))

(def in-channel
  "### in-channel
   This is the input channel from the Il-2 dedicated server console. It receives values
   one line at a time from the server socket."
  (chan 10))

(def mult-channel
  "### mult-channel
   This mult channel filters the input values from in-channel above. We filter the
   channel to accept only lines which don't begin with the server console prompt
   text, so that only genuine output from the console is used in later processing."
  (mult
    (remove<
      #(re-matches #"<consoleN><\d++>" %)
      in-channel)))

(def event-channel
  (chan 10))

(def print-channel
  "### print-channel
   This channel taps mult-channel, and is used to print directly to the server console
   text area control."
  (tap mult-channel (chan 10)))

(def diff-pipe
  "### diff-channel
   This channel taps mult-channel, and filters for lines which consist of two
   spaces, followed by a single word, followed by a single number which is either
   0 or 1, followed by a newline character. This is the exact format used to print
   difficulty settings by the server console.

   This channel is used to parse difficulty settings from the server console output."
  (filter-pipe
    mult-channel
    event-channel
    :diff
    #"\s{2}\w+\s*+[0-1]\n"))

(def mis-pipe
  "### mis-channel
   This channel taps mult-channel, and filters for lines which contain the three
   mission status lines. This channel is used to parse mission load, begin, end
   and unload events from the server console output."
  (filter-pipe
    mult-channel
    event-channel
    :mis
    #"(Mission){1}:?+\s.+\S++\n"))

(def pilot-pipe
  "### pilot-channel"
  (filter-pipe
    mult-channel
    event-channel
    :pilot
    #"(socket channel){1}\s'\d++'.+is complete created\n"
    #"(socketConnection with){1}.+on channel \d++ lost\..+\n"))

(def ban-pipe
  (filter-pipe
    mult-channel
    event-channel
    :ban
    #"\s{2}.+\n"))

(def user-pipe
  (filter-pipe
    mult-channel
    event-channel
    :user
    #"\s\d++\s++.+\s++\d++\s++\d++\s++\(\d\).+\n"))

(def host-pipe
  (filter-pipe
    mult-channel
    event-channel
    :host
    #"\s\d++:\s.+\s\[\d++\](\d{1,3}\.?){4}:\d++\n"))

(def error-pipe
  "### err-channel
   This channel taps mult-channel, and filters for lines which contain the
   mission load error text. This channel is used to set the mission status
   to unloaded when the user tries to load an invalid mission path."
  (filter-pipe
    mult-channel
    event-channel
    :error
    #"ERROR mission:.+NOT loaded" ))

(defn close-channels
  "### close-channels
   This function closes all channels so that pending operations don't block
   and can end successfully."
  []
  (close! in-channel)
  (close! event-channel))