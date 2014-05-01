;;
;; ## Server connection and I/O functions
;;
;; Here we define functions to interact with the Il-2 server console socket. This
;; includes functions to write to and read from the socket, along with a socket
;; listener function to put all non-nil values into the channel from which all
;; server state processing in the UI is driven.
(ns uk.org.il2ssd.server
  (:require [clojure.core.async :refer [>!! thread]]
            [uk.org.il2ssd.channel :refer :all]
            [uk.org.il2ssd.state :as state])
  (:import (java.io BufferedReader BufferedWriter InputStreamReader
                    OutputStreamWriter PrintWriter)
           (java.net ConnectException InetSocketAddress Socket
                     SocketTimeoutException SocketException)
           (java.nio.charset Charset)
           (org.apache.commons.lang StringEscapeUtils)))

;; ### Socket atoms
;; Here we define atoms to access the specific instances of the Socket, PrintWriter
;; and BufferedReader objects that were created on this connection attempt outside
;; of the initial connection function.
(def socket (atom nil))
(def socket-in (atom nil))
(def socket-out (atom nil))

(defn socket-listener
  "### socket-listener
   This function spawns a new thread on which we repeatedly perform a read from
   the server socket for as long as our connection state atom says that we are
   connected.

   Any time that we read a non-nil string from the socket, we unescape any
   Unicode characters using Apache Commons StringEscapeUtils and put the
   resulting text into in-channel, the channel that is used as the source for
   all server state parsing."
  []
  (thread (while @state/connected
            (when-let [text (try (.readLine ^BufferedReader @socket-in)
                                 (catch SocketException _ nil))]
              (->> text
                   (StringEscapeUtils/unescapeJava)
                   (>!! in-channel))))))

(defn write-socket
  "### write-socket
   This is a one argument function that simply calls the print method of the
   object that is stored in the socket-out atom using the argument that we
   provide.

   The argument is coerced to a String and a newline character is
   appended before printing. The output stream is then flushed.

   This atom should contain the instance of the PrintWriter object that is
   instantiated when we successfully connect to the server."
  [text]
  (.print ^PrintWriter @socket-out (str text "\n"))
  (.flush ^PrintWriter @socket-out))

(defn get-server-text
  "### get-server-text
   This is a zero argument function which simply sends the command to the server
   console which returns the server description text to the console output using
   the write-socket function defined above."
  []
  (write-socket "server"))

(defn get-difficulty
  "### get-difficulty
   This is a zero argument function which sends the command to return all
   difficulty settings to the server console output."
  []
  (write-socket "difficulty"))

(defn get-mission-state
  "### get-difficulty
   This is a zero argument function which sends the command to return the
   current mission state to the server console output."
  []
  (write-socket "mission"))

(defn load-mission
  "### load-mission
   This is a one argument function which sends the command to the server console
   which loads a mission using the path described by the argument."
  [path-to-mission]
  (write-socket (str "mission LOAD " path-to-mission)))

(defn load-begin-mission
  "### load-begin-mission
   This one argument function sends a command to the server console to load and
   start the mission provided in the argument."
  [path-to-mission]
  (write-socket (str "mission LOAD " path-to-mission " BEGIN")))

(defn unload-mission
  "### unload-mission
   This is a zero argument function which sends the command to the server console
   which unloads the current mission.

   Because the unload command doesn't return any output on completion, we also
   request the mission state so that the state parsers can register the change in
   mission status."
  []
  (write-socket "mission DESTROY")
  (get-mission-state))

(defn start-mission
  "### start-mission
   This is a zero-argument function which sends the command to the server console
   which starts the currently loaded mission."
  []
  (write-socket "mission BEGIN"))

(defn end-mission
  "### unload-mission
   This is a zero argument function which sends the command to the server console
   which ends the current mission.

   Because the end command doesn't return any output on completion, we also request
   the mission state so that the state parsers can register the change in mission
   status."
  []
  (write-socket "mission END")
  (get-mission-state))

(defn set-difficulty
  "### set-difficulty
   This is a two argument function which sends the command to the server console
   which sets the difficulty setting provided in the first argument to the value
   provided in the second argument."
  [setting value]
  (write-socket (str "difficulty " setting " " value)))

(defn connect
  "### connect
   This is a two argument function which instantiates an InetSocketAddress using
   the provided host and port arguments. It then stores a new Socket instance
   in the socket atom defined above and uses the InetSocketAddress to connect to
   the Socket.

   After connecting, we put a new BufferedReader instance that wraps the input
   stream for the socket into the socket-in atom and put a new PrintWriter
   instance which wraps the output stream for the socket into the socket-out
   atom.

   We also set the connected state atom to true and start the socket-listener
   to begin parsing the server state. We also get the server description text
   and mission status here so that there is something display in the UI as
   a kind of welcome text.

   We use a timeout on the socket connect attempt and wrap the connection and
   reader/writer instantiation attempts in a try/catch block to catch any I/O
   exceptions which result."
  [host port]
  (let [address (InetSocketAddress. ^String host ^int port)]
    (reset! socket (Socket.))
    (try (.connect ^Socket @socket address 10000)
         (reset! socket-in (BufferedReader.
                             (InputStreamReader.
                               (.getInputStream ^Socket @socket)
                               (Charset/forName "UTF-8"))))
         (reset! socket-out (PrintWriter.
                              (BufferedWriter.
                                (OutputStreamWriter.
                                  (.getOutputStream ^Socket @socket)
                                  (Charset/forName "UTF-8")))
                              true))
         (reset! state/connected true)
         (socket-listener)
         (get-server-text)
         (get-mission-state)
         (catch ConnectException _ nil)
         (catch SocketTimeoutException _ nil))))

(defn disconnect
  "### disconnect
   This is a zero argument function, which simply resets all of the global state
   values to their initial values and closes our socket.

   We are forced to call the shutdownInput method on the socket so that read
   attempts return nil and any reads that are currently blocking return this
   nil value.

   This allows the socket-listener thread to get to the connected
   state evaluation and end rather than staying at a running, blocked state."
  []
  (reset! state/loading false)
  (reset! state/loaded false)
  (reset! state/playing false)
  (reset! state/cycle-running false)
  (reset! state/dcg-running false)
  (reset! state/connected false)
  (.shutdownInput ^Socket @socket)
  (.close ^Socket @socket))

