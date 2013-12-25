;;;;
;;;; Settings functions
;;;;
(ns uk.org.il2ssd.settings
    (:import [java.io FileNotFoundException])
    (:require [clojure.string :as string]
              [uk.org.il2ssd.jfx :as jfx]))

(def difficulties (atom {}))

(def mission (atom nil))

(def server (atom nil))

(def config-file (atom nil))

(defn parse-file [] ())

(defn build-file [] (let [file (atom [])
                          newln (System/lineSeparator)]
                        (swap! file conj "[Il-2 Simple Server Daemon]")
                        (swap! file conj (str newln "[Mission]"))
                        (if (seq @mission)
                            (doseq [setting @mission]
                                (let [[key value] setting]
                                    (swap! file conj (str key " = " setting)))))
                        (swap! file conj (str newln "[Server]"))
                        (if (seq @server)
                            (doseq [setting @server]
                                (let [[key value] setting]
                                    (swap! file conj (str key " = " setting)))))
                        (println @file)
                        (reset! config-file (string/join newln @file))
                        (println @config-file)))

(defn save-to-file [] (do (build-file)
                          (spit "il2ssd.ini" @config-file)))

(defn load-from-file [] (try
                            (->> (slurp "il2ssd.ini")
                                (reset! config-file))
                            (catch FileNotFoundException e (println "File not found."))))

(defn save-state [] ())