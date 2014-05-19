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
                                        tap untap-all]]))

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

(def print-channel
  "### print-channel
   This channel taps mult-channel, and is used to print directly to the server console
   text area control."
  (tap mult-channel (chan 10)))

(def diff-channel
  "### diff-channel
   This channel taps mult-channel, and filters for lines which consist of two
   spaces, followed by a single word, followed by a single number which is either
   0 or 1, followed by a newline character. This is the exact format used to print
   difficulty settings by the server console.

   This channel is used to parse difficulty settings from the server console output."
  (filter<
    #(re-matches #"\s{2}\w+\s*+[0-1]\n" %)
    (tap mult-channel (chan 10))))

(def mis-channel
  "### mis-channel
   This channel taps mult-channel, and filters for lines which contain the three
   mission status lines. This channel is used to parse mission load, begin, end
   and unload events from the server console output."
  (filter<
    #(re-matches #"(Mission){1}:?+\s.+\S++\n" %)
    (tap mult-channel (chan 10))))

(def pilot-channel
  "### pilot-channel"
  (filter<
    #(or (re-matches #"(socket channel){1}\s'\d++'.+is complete created\n" %)
         (re-matches #"(socketConnection with){1}.+on channel \d++ lost\..+\n" %))
    (tap mult-channel (chan 10))))

(def ban-channel
  (filter<
    #(re-matches #"\s{2}.+\n" %)
    (tap mult-channel (chan 10))))

(def user-channel
  (filter<
    #(re-matches #"\s\d++\s++.+\s++\d++\s++\d++\s++\(\d\).+\n" %)
    (tap mult-channel (chan 10))))

(def host-channel
  (filter<
    #(re-matches #"\s\d++:\s.+\s\[\d++\](\d{1,3}\.?){4}:\d++\n" %)
    (tap mult-channel (chan 10))))

(def err-channel
  "### err-channel
   This channel taps mult-channel, and filters for lines which contain the
   mission load error text. This channel is used to set the mission status
   to unloaded when the user tries to load an invalid mission path."
  (filter<
    #(re-find #"ERROR mission:.+NOT loaded" %)
    (tap mult-channel (chan 10))))

(defn close-channels
  "### close-channels
   This function untaps the mult and closes all channels so that pending
   operations don't block and can end successfully."
  []
  (untap-all mult-channel)
  (close! in-channel)
  (close! print-channel)
  (close! diff-channel)
  (close! mis-channel)
  (close! err-channel)
  (close! pilot-channel)
  (close! ban-channel)
  (close! user-channel)
  (close! host-channel))