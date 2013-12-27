;;;;
;;;; Settings functions
;;;;
(ns uk.org.il2ssd.settings

    (:import [java.io FileNotFoundException])

    (:require [clojure.string :as string]
              [uk.org.il2ssd.parse :as parse]))

(def difficulty-settings (atom {}))

(def mission-settings (atom nil))

(def server-settings (atom nil))

(def config-file (atom nil))

(defn parse-file [] ())

(defn build-file [] (let [file (atom [])
                          newln (System/lineSeparator)]
                        (swap! file conj "[Il-2 Simple Server Daemon]")
                        (swap! file conj (str newln "[Mission]"))
                        (if (seq @mission-settings)
                            (doseq [setting @mission-settings]
                                (let [[key value] setting]
                                    (swap! file conj (str key " = " value)))))
                        (swap! file conj (str newln "[Server]"))
                        (if (seq @server-settings)
                            (doseq [setting @server-settings]
                                (let [[key value] setting]
                                    (swap! file conj (str key " = " value)))))
                        (reset! config-file (string/join newln @file))))

(defn save-to-file [] (do (build-file)
                          (spit "il2ssd.ini" @config-file)))

(defn read-from-file [] (try
                            (do (->> (slurp "il2ssd.ini")
                                    (reset! config-file))
                                (if @config-file
                                    (let [parsed (parse/file-parser @config-file)]
                                        (println parsed))))
                            (catch FileNotFoundException e (println "File not found."))))

(defn save-server [ip port]
    (swap! server-settings assoc "IP" ip)
    (swap! server-settings assoc "Port" port))

(defn save-mission
    ([mode] (swap! mission-settings assoc "Mode" mode))
    ([mode mission])
    ([mode mission cycle]))