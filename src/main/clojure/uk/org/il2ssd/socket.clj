;;;;
;;;; Socket connection and IO functions
;;;;
(ns uk.org.il2ssd.socket

    (:require [clojure.core.async :refer [thread >!!]]
              [uk.org.il2ssd.channel :refer :all]
              [uk.org.il2ssd.parse :as parse]
              [uk.org.il2ssd.state :as state])

    (:import (org.apache.commons.lang StringEscapeUtils)
             (java.net InetSocketAddress Socket SocketTimeoutException)
             (java.io BufferedReader InputStreamReader PrintWriter)
             (java.nio.charset Charset)))

(def socket (atom nil))
(def socket-in (atom nil))
(def socket-out (atom nil))

(defn read-service []
    (thread (while @state/connected
                (let [text (.readLine @socket-in)]
                    (if (not= text nil)
                        (->> text
                            (StringEscapeUtils/unescapeJava)
                            (>!! in-channel)))))))

(defn write-socket [text]
    (.println @socket-out text))

(defn get-server-text [] (write-socket "server"))

(defn get-difficulty [] (write-socket "difficulty"))

(defn get-mission-state [] (write-socket "mission"))

(defn load-mission [path-to-mission] (write-socket (str "mission LOAD " path-to-mission)))

(defn start-mission [] (write-socket "mission BEGIN"))

(defn end-mission [] (write-socket "mission END"))

(defn unload-mission [] (write-socket "mission DESTROY"))

(defn set-difficulty [setting value] (write-socket (str "difficulty " setting " " value)))

(defn connect [host port] (let [address (InetSocketAddress. host port)]
                              (reset! socket (Socket.))
                              (.connect @socket address 10000)
                              (reset! socket-in (BufferedReader.
                                                    (InputStreamReader.
                                                        (.getInputStream @socket) (Charset/forName "UTF-8"))))
                              (reset! socket-out (PrintWriter. (.getOutputStream @socket) true))
                              (reset! state/connected true)
                              (get-difficulty)
                              (get-server-text)
                              (get-mission-state)
                              (read-service)
                              (parse/parse-difficulty)))
(defn disconnect []
    (reset! state/connected false)
    (.shutdownInput @socket)
    (.flush @socket-out)
    (.close @socket-out)
    (.close @socket-in)
    (.close @socket))

