;;;;
;;;; Settings functions
;;;;
(ns uk.org.il2ssd.settings

  (:import [java.io FileNotFoundException File])

  (:require [clojure.string :as string]
            [com.brainbot.iniconfig :as iniconfig]))

(def difficulty-settings (atom {}))

(def mission-settings
  "Atom to hold a map of the mission settings at runtime."
  (atom nil))

(def mission-cycle
  "Atom to hold a vector of mission maps."
  (atom []))

(def server-settings
  "Atom to hold a map of the server settings at runtime."
  (atom nil))

(def difficulty-file
  "Atom to hold a built difficulty file."
  (atom nil))

(def saved-difficulties
  "Atom to hold difficulties loaded from a difficulty file."
  (atom nil))

(defn save-server
  "Multiple-arity function for saving server connection & path details."
  ([ip port]
   (swap! server-settings assoc "IP" ip)
   (swap! server-settings assoc "Port" port))
  ([path]
   (if (.isFile (File. ^String path))
     (swap! server-settings assoc "Path" path)))
  ([ip port path]
   (swap! server-settings assoc "IP" ip)
   (swap! server-settings assoc "Port" port)
   (if (.isFile (File. ^String path))
     (swap! server-settings assoc "Path" path))))

(defn save-mission
  "Multiple-arity function for saving mission details."
  ([mode] (swap! mission-settings assoc "Mode" mode))
  ([mode mission]
   (swap! mission-settings assoc "Mode" mode)
   (swap! mission-settings assoc "Mission" mission))
  ([index mission timer]
   (swap! mission-cycle assoc-in [index] (sorted-map :mission mission :timer timer))
   (swap! mission-settings assoc "Cycle" @mission-cycle)))

(defn build-config-file []
  "Builds an .ini config file to save each of the key-value pair settings atoms."
  (let [file (atom [])
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
    (reset! file (string/join newln @file))))

(defn save-config-file
  "Saves a config file to the current working directory as \"il2ssd.ini\"."
  []
  (->> (build-config-file)
       (spit "il2ssd.ini")))

(defn read-config-file
  "Reads il2ssd.ini from the current working directory. Uses the iniconfig library to return a map of key-value pairs."
  []
  (try
    (iniconfig/read-ini "il2ssd.ini")
    (catch FileNotFoundException e nil)))


(defn build-difficulty-file
  "Builds a difficulty setting file using the host parameter to indicate from which server it was saved."
  [host]
  (let [file (atom [])
        newln (System/lineSeparator)]
    (swap! file conj "# Difficulty File")
    (swap! file conj (str "# saved from server" host))
    (swap! file conj (str newln "[Difficulty]"))
    (if (seq @difficulty-settings)
      (doseq [setting @difficulty-settings]
        (let [[key value] setting]
          (swap! file conj (str key " = " value)))))
    (reset! file (string/join newln))))

(defn save-difficulty-file
  "Using the host parameter, saves a built difficulty file as \"<host>-difficulty.ini\"."
  [host]
  (do (build-difficulty-file host)
      (spit (str host "-difficulty.ini"))))

(defn read-difficulty-file
  "Reads a difficulty file from the path parameter."
  [path-to-file]
  (try
    (iniconfig/read-ini path-to-file)
    (catch FileNotFoundException e nil)))
