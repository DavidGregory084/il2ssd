;;
;; ## Settings functions
;;
;; Here we define functions to build config files and to save and load these
;; files.
(ns uk.org.il2ssd.settings

  (:import [java.io FileNotFoundException File]
           (clojure.lang PersistentArrayMap PersistentVector))

  (:require [clojure.string :as string]
            [com.brainbot.iniconfig :as iniconfig]))

(def difficulty-settings
  "### difficulty-settings
   This is an atom to hold a map of the server difficulty settings so that they
   can be saved in a difficulty file."
  (atom {}))

(def mission-settings
  "### mission-settings
   This is an atom to hold a map of the mission settings so that they can be
   saved in the main \"il2ssd.ini\" config file."
  (atom nil))

(def mission-cycle
  "### mission-cycle
   This is an atom to hold a vector of the missions in the current mission cycle
   so that they can be saved in the main \"il2ssd.ini\" config file. It is
   intended that these cycles will be saved separately as a further development."
  (atom nil))

(def server-settings
  "### server-settings
   This is an atom to hold a map of the server settings so that they can be saved
   in the main \"il2ssd.ini\" config file."
  (atom nil))

(def saved-difficulties
  "### saved-difficulties
   This is an atom to hold the difficulty settings loaded from a saved difficulty
   file, so that they can be loaded into the UI and set on the server from these
   files."
  (atom nil))

(defprotocol BuildSettings
  (build-conf [this file] [this file newln?]))

(extend-protocol BuildSettings

  nil
  (build-conf
    ([this file] file)
    ([this newln? file] file))

  String
  (build-conf [this newln? file]
    (let [newln (System/lineSeparator)]
      (if newln?
        (conj file (str newln this))
        (conj file this))))

  PersistentArrayMap
  (build-conf [this file]
    (if (seq this)
      (reduce conj file
              (for [setting this
                    :let [[key value] setting]]
                (str key " = " value)))
      file))

  PersistentVector
  (build-conf [this file]
    (if (seq this)
      (reduce conj file
              (for [setting (map-indexed #(vector (inc %) %2) this)
                    :let [[key value] setting]]
                (string/replace (str key " = " value) "\"" "")))
      file)))

(defn save-server
  "### save-server
   This is a multiple arity function that saves the provided arguments into the
   server-settings atom. If a path argument is provided, it determines whether
   the path is valid before saving the path."
  ([path]
   (if (.isFile (File. ^String path))
     (swap! server-settings assoc "Path" path)))
  ([ip port]
   (swap! server-settings assoc "IP" ip)
   (swap! server-settings assoc "Port" port))
  ([ip port path]
   (swap! server-settings assoc "IP" ip)
   (swap! server-settings assoc "Port" port)
   (when (.isFile (File. ^String path))
     (swap! server-settings assoc "Path" path))))

(defn save-mission
  "### save-mission
   This is a multiple-arity function to save the provided arguments into the
   mission-settings atom. If one or two arguments is provided the mode and
   mission status can be saved."
  ([mode] (swap! mission-settings assoc "Mode" mode))
  ([mode mission]
   (swap! mission-settings assoc "Mode" mode)
   (when (.isFile (File. ^String mission))
     (swap! mission-settings assoc "Single Mission" mission))))

(defn save-cycle
  "### save-cycle
   This is a multiple-arity function to save mission cycle data. When no arguments
   are provided it is assumed that the mission cycle has been saved in the mission
   cycle atom and should be added to the mission settings atom.

   When three arguments are provided it is assumed that a mission cycle is being
   saved and the three arguments define the index that is assigned to the mission
   in the mission-cycle vector, the mission path that is to be saved and the
   timer that is applied to the mission."
  ([]
   (swap! mission-settings assoc "Cycle" @mission-cycle))
  ([index mission timer]
   (swap! mission-cycle assoc-in [index] [mission timer])))

(defn build-config-file
  "### build-config-file
   This is a zero argument function which builds a config file for the program.
   It first defines an empty vector atom and retrieves the system default line
   separator.

   Then the file is built by using conj to add strings to the end of the vector.
   First, a header comment is added. Then, a section header is written. We doseq
   over the atom which contains the settings for this section, destructuring the
   contents of each setting into a key-value pair to be saved into the file.

   Whenever we wish to add a blank line to the output (e.g. before section
   headers), we simply add the system default line separator to the start of the
   string that will follow this blank line in the file.

   We repeat this process for each section in the file.

   At the end, we use join from clojure.string to join the vector into a single
   string using the system default line separator.

   We then return this string to the calling function."
  []
  (let [newln (System/lineSeparator)]
    (->> []
         (build-conf "# Il-2 Simple Server Daemon" false)
         (build-conf "[Server]" true)
         (build-conf @server-settings)
         (build-conf "[Mission]" true)
         (build-conf @mission-settings)
         (build-conf "[Cycle]" true)
         (build-conf @mission-cycle)
         (string/join newln))))

(defn save-config-file
  "### save-config-file
   This is a zero argument function which calls build-config-file and saves the
   results to an il2ssd.ini file in the program's current working directory.

   If this file already exists, it is simply replaced."
  []
  (->> (build-config-file)
       (spit "il2ssd.ini")))

(defn read-config-file
  "### read-config-file
   This is a zero argument function which reads the \"il2ssd.ini\" config file
   from the current working directory and parses it into a nested map of
   key-value pairs using the iniconfig library. Section headers form the
   top-level keys and each section consists of a map of key-value pairs from
   the file where the keyword used is the name of the setting.

   Finally, we return this map to the calling function.

   We wrap the read-ini function in a try/catch block so that we catch the
   FileNotFoundException that will result if the file doesn't exist."
  []
  (try
    (iniconfig/read-ini "il2ssd.ini")
    (catch FileNotFoundException e nil)))

(defn get-configuration
  [file]
  (hash-map
    :ip-field (get-in file ["Server" "IP"] "")
    :port-field (get-in file ["Server" "Port"] "")
    :server-path-lbl (get-in file ["Server" "Path"] "...")
    :mode-choice (get-in file ["Mission" "Mode"] "single")
    :single-path-lbl (get-in file ["Mission" "Single Mission"] "...")))