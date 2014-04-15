;;
;; ## Config functions
;;
;; Here we define functions to build config files and to save and load these
;; files.
(ns uk.org.il2ssd.config
  (:require [clojure.string :as string]
            [com.brainbot.iniconfig :as iniconfig])
  (:import (clojure.lang PersistentArrayMap PersistentVector)
           (java.io File FileNotFoundException)))

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
  (build-conf [this file] [this newln? file]))

;; ### BuildSettings
;; Here we extend the protocol defined above to handle building a config file vector
;; using type-based dispatch to handle each data structure.
;;
;; Strings can be passed with an extra parameter which determines whether they are
;; preceded by a newline character.
;;
;; Maps are used to produce simple key-value pairs in the file.
;;
;; Vectors are reserved for saving mission cycle data. In this case we create
;; key-value pairs keyed by the index of the mission in the vector (incremented by
;; one). The value is taken from the vector nested within the main vector at this
;; index. The output takes the following form:
;;
;;     1 = ["mission path" "timer"]
(extend-protocol BuildSettings

  nil
  (build-conf
    ([this file] file)
    ([this newln? file] file))

  String
  (build-conf
    ([this file]
     (conj file this))
    ([this newln? file]
     (let [newln (System/lineSeparator)]
       (if newln?
         (conj file (str newln this))
         (conj file this)))))

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
   (when (.isFile (File. ^String path))
     (swap! server-settings assoc "Path" path)))
  ([ip port]
   (when (not (string/blank? ip))
     (swap! server-settings assoc "IP" ip))
   (when (not (string/blank? port))
     (swap! server-settings assoc "Port" port)))
  ([ip port path]
   (when (not (string/blank? ip))
     (swap! server-settings assoc "IP" ip))
   (when (not (string/blank? port))
     (swap! server-settings assoc "Port" port))
   (when (.isFile (File. ^String path))
     (swap! server-settings assoc "Path" path))))

(defn save-mission
  "### save-mission
   This is a multiple-arity function to save the provided arguments into the
   mission-settings atom. If one argument is provided the mode can be saved.
   Passing two arguments allows both mode and single mission path to be saved."
  ([mode]
   (swap! mission-settings assoc "Mode" mode))
  ([mode mission]
   (swap! mission-settings assoc "Mode" mode)
   (when (not= mission "...")
     (swap! mission-settings assoc "Single Mission" mission))))

(defn save-cycle
  "### save-cycle
   This is a three argument function to save mission cycle data. The three
   arguments define the index that is assigned to the mission in the mission
   cycle vector, the mission path that is to be saved and the timer that is
   applied to the mission."
  [index mission timer]
  (swap! mission-cycle assoc-in [index] [mission timer]))

(defn build-config-file
  "### build-config-file
   This is a zero argument function which builds a config file for the program.
   It first retrieves the system default line separator.

   Then the file is built by using conj to add strings to an empty vector,
   which is passed through a series of appending calls to build-conf.

   These calls will build the config file accordingly based upon the type of the
   value passed.

   First, a header comment is added. Then, a section header is written.

   For each section, we call build-conf, passing the contents of the settings
   atom for this section.

   Whenever we wish to add a blank line to the output (e.g. before section
   headers), we simply pass an extra parameter to build-conf with the header
   to prepend the string values with a newline character.

   We repeat this process for each section in the file.

   At the end, we use join from clojure.string to join the vector into a single
   string using the system default line separator as a separator character.

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
  "### get-configuration
   This one argument function takes values from the nested data structure
   retrieved from the config file and binds them to keys which name the
   controls which will receive these values. This data structure is the
   only required parameter."
  [file]
  (hash-map
    :ip-field (get-in file ["Server" "IP"] "")
    :port-field (get-in file ["Server" "Port"] "")
    :server-path-lbl (get-in file ["Server" "Path"] "...")
    :mode-choice (get-in file ["Mission" "Mode"] "single")
    :single-path-lbl (get-in file ["Mission" "Single Mission"] "...")))