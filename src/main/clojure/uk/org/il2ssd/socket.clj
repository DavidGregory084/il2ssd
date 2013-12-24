;;;;
;;;; Socket connection functions
;;;;
(ns uk.org.il2ssd.socket
    (:require [clojure.core.async :refer [go thread >! >!!]]
              [uk.org.il2ssd.channel :refer :all])
    (:import (org.apache.commons.lang StringEscapeUtils)
             (java.net InetSocketAddress Socket SocketTimeoutException)
             (java.io BufferedReader InputStreamReader PrintWriter)
             (java.nio.charset Charset)))

(def socket (atom nil))
(def socket-in (atom nil))
(def socket-out (atom nil))
(def connected (atom nil))

(defn read-service []
            (thread (while @connected
                        (let [text (.readLine @socket-in)]
                            (if (not= text nil)
                                (->> text
                                    (StringEscapeUtils/unescapeJava)
                                    (>!! in-channel)))))))

(defn write-socket [text]
        (.println @socket-out text))

(defn connect [host port] (let [address (InetSocketAddress. host port)]
                              (reset! socket (Socket.))
                              (.connect @socket address 10000)
                              (reset! socket-in (BufferedReader.
                                                    (InputStreamReader.
                                                        (.getInputStream @socket) (Charset/forName "UTF-8"))))
                              (reset! socket-out (PrintWriter. (.getOutputStream @socket) true))
                              (reset! connected true)
                              (write-socket "server")))
(defn disconnect []
    (reset! connected false)
    (.shutdownInput @socket)
    (.flush @socket-out)
    (.close @socket-out)
    (.close @socket-in)
    (.close @socket))

