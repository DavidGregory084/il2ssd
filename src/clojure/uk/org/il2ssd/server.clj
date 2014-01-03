;;;;
;;;; Server connection and IO functions
;;;;
(ns uk.org.il2ssd.server

  (:require [clojure.core.async :refer [thread >!!]]
            [uk.org.il2ssd.channel :refer :all]
            [uk.org.il2ssd.parse :as parse]
            [uk.org.il2ssd.settings :as settings]
            [uk.org.il2ssd.state :as state])

  (:import (org.apache.commons.lang StringEscapeUtils)
           (java.net InetSocketAddress Socket SocketTimeoutException ConnectException)
           (java.io BufferedReader InputStreamReader PrintWriter)
           (java.nio.charset Charset)))

(def socket (atom nil))
(def socket-in (atom nil))
(def socket-out (atom nil))

(defn socket-listener
  "While connected, reads the server socket input stream and puts any non-nil input into the parser input channel after unescaping Unicode characters."
  []
  (thread (while @state/connected
            (let [text (.readLine ^BufferedReader @socket-in)]
              (if (not= text nil)
                (->> text
                     (StringEscapeUtils/unescapeJava)
                     (>!! in-channel)))))))

(defn write-socket
  "Prints the provided string to the server command line."
  [text]
  (.println ^PrintWriter @socket-out text))

(defn get-server-text
  "Gets the server info text."
  []
  (write-socket "server"))

(defn get-difficulty
  "Requests all difficulty settings and their values."
  []
  (write-socket "difficulty"))

(defn get-mission-state
  "Requests the mission state."
  []
  (write-socket "mission"))

(defn load-mission
  "Loads a mission using the path string parameter."
  [path-to-mission]
  (write-socket (str "mission LOAD " path-to-mission)))

(defn unload-mission
  "Unloads the currently loaded mission and gets the mission state."
  []
  (write-socket "mission DESTROY")
  (get-mission-state))

(defn start-mission
  "Starts the currently loaded mission."
  []
  (write-socket "mission BEGIN"))

(defn end-mission
  "Ends the running mission."
  []
  (write-socket "mission END")
  (get-mission-state))

(defn set-difficulty
  "Sets the difficulty setting using the provided arguments."
  [setting value]
  (write-socket (str "difficulty " setting " " value)))

(defn connect
  "Open a TCP socket connection on the specified hostname and port. Starts the socket-listener service."
  [host port] (let [address (InetSocketAddress. ^String host ^int port)]
                (reset! socket (Socket.))
                (try (.connect ^Socket @socket address 10000)
                     (reset! socket-in (BufferedReader.
                                         (InputStreamReader.
                                           (.getInputStream ^Socket @socket) (Charset/forName "UTF-8"))))
                     (reset! socket-out (PrintWriter. (.getOutputStream ^Socket @socket) true))
                     (reset! state/connected true)
                     (socket-listener)
                     (get-server-text)
                     (get-mission-state)
                     (catch ConnectException e nil)
                     (catch SocketTimeoutException e nil))))
(defn disconnect
  "Resets all global state atoms and closes the socket."
  []
  (reset! state/loaded false)
  (reset! state/playing false)
  (reset! state/connected false)
  (.shutdownInput ^Socket @socket)
  (.flush ^PrintWriter @socket-out)
  (.close ^PrintWriter @socket-out)
  (.close ^BufferedReader @socket-in)
  (.close ^Socket @socket))

