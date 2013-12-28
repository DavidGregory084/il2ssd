;;;;
;;;; Settings functions
;;;;
(ns uk.org.il2ssd.settings

    (:import [java.io FileNotFoundException])

    (:require [clojure.string :as string]
              [com.brainbot.iniconfig :as iniconfig]))

(def difficulty-settings (atom {}))

(def mission-settings (atom nil))

(def server-settings (atom nil))

(def config-file (atom nil))

(def difficulty-file (atom nil))

(def saved-difficulties (atom nil))

(defn build-file [] (let [file (atom [])
                          newln (System/lineSeparator)]
                        (swap! file conj "# Il-2 Simple Server Daemon")
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

(defn build-difficulty-file [host] (let [file (atom [])
                          newln (System/lineSeparator)]
                                   (swap! file conj "# Difficulty File")
                                   (swap! file conj (str "# saved from server" host))
                                   (swap! file conj (str newln "[Difficulty]"))
                                   (if (seq @difficulty-settings)
                                       (doseq [setting @difficulty-settings]
                                           (let [[key value] setting]
                                               (swap! file conj (str key " = " value)))))
                                   (reset! difficulty-file (string/join newln @file))))

(defn save-difficulty-file [host] (do (build-difficulty-file host)
                                      (spit (str host "-difficulty.ini"))))

(defn save-to-file [] (do (build-file)
                          (spit "il2ssd.ini" @config-file)))

(defn read-from-file []
    (try
        (iniconfig/read-ini "il2ssd.ini")
        (catch FileNotFoundException e nil)))

(defn read-difficulty-file [path-to-file]
    (try
        (iniconfig/read-ini path-to-file)
        (catch FileNotFoundException e nil)))

(defn save-server
    ([ip port]
        (swap! server-settings assoc "IP" ip)
        (swap! server-settings assoc "Port" port))
    ([path]
        (swap! server-settings assoc "Path" path))
    ([ip port path]
        (swap! server-settings assoc "IP" ip)
        (swap! server-settings assoc "Port" port)
        (swap! server-settings assoc "Path" path)))

(defn save-mission
    ([mode] (swap! mission-settings assoc "Mode" mode))
    ([mode mission])
    ([mode mission cycle]))