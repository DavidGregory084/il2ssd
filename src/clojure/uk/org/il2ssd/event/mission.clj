(ns uk.org.il2ssd.event.mission
  (:require [clojure.set :refer [map-invert]]
            [clojure.string :as string]
            [uk.org.il2ssd.jfx.ui :as ui]
            [uk.org.il2ssd.server :as server]
            [uk.org.il2ssd.state :as state])
  (:import (java.io File)
           (java.nio.file Path Paths)))

(defn mis-selected?
  "### mis-selected?
   This zero argument function is used to determine whether there is a valid
   mission selection, dependent upon the current UI mission loading mode.
   It does this by retrieving the contents of the valid control for the mission
   mode selected."
  [mode]
  (let [{:keys [single-path-lbl
                cycle-data]} @state/control-instances
        single-mis (ui/get-text single-path-lbl)]
    (case mode
      "single" (if-not (or (string/blank? single-mis)
                           (= single-mis "..."))
                 true
                 false)
      "cycle" (if (> (ui/get-list-size cycle-data) 0)
                true
                false)
      "dcg" false)))

(defn get-relative-path
  "### get-relative-path
   This two-argument function returns a relative path for a mission based upon
   the two input paths.

   The server path is expected to be the string path to the il2server.exe file.

   The mission path should be the full canonical path string of the mission file
   we wish to load.

   In order to determine the path to send to the server we convert the strings
   to Path objects, get the parent path of the server .exe file (which should be
   the main server directory), and resolve this against the string \"Missions\".

   This Path object should represent the Missions directory of our server.

   Finally, we relativise the input path against the Missions directory path to
   get a relative path to the mission from the Missions directory."
  [server-path mis-path]
  (let [in-path (Paths/get mis-path (into-array String []))
        server-exe (Paths/get server-path (into-array String []))
        server-dir (.getParent server-exe)
        mis-dir (.resolve server-dir "Missions")
        out-path (->> in-path (.relativize mis-dir) str)]
    (string/replace out-path "\\" "/")))

(defn mode-choice
  "### mode-choice
   This is a one argument function which uses the content of the modes argument to
   retrieve the key of the mode text selected.

   This key is used to load the correct UI pane for the mission mode the user has
   selected.

   The mission state atoms are also set conditional upon a valid mission selection
   for the mode chosen."
  [modes]
  (let [{:keys [mission-pane
                single-mis-pane
                cycle-mis-pane
                mode-choice
                load-btn
                tool-bar
                start-btn
                cycle-start-btn
                single-path-lbl
                cycle-data]} @state/control-instances
        mode (name ((map-invert modes) (ui/get-choice mode-choice)))]
    (reset! state/mode mode)
    (when (= mode "single")
      (if (mis-selected? mode)
        (reset! state/single-mission-path
                (ui/get-text single-path-lbl))
        (reset! state/single-mission-path nil))
      (ui/set-visible load-btn true)
      (ui/swap-start-button tool-bar cycle-start-btn start-btn)
      (ui/set-mis-pane mission-pane single-mis-pane))
    (when (= mode "cycle")
      (if (mis-selected? mode)
        (reset! state/cycle-mission-path
                (:mission (ui/get-cycle-mission cycle-data @state/cycle-index)))
        (reset! state/cycle-mission-path nil))
      (ui/set-visible load-btn false)
      (ui/swap-start-button tool-bar start-btn cycle-start-btn)
      (ui/set-mis-pane mission-pane cycle-mis-pane))))

(defn set-single-remote
  "### set-single-remote
   This zero argument function sets the UI label which defines the mission
   to load based upon the content of the remote single mission text field.

   This is used when the user does not have local access to the server
   mission files."
  []
  (let [{:keys [single-path-fld
                single-path-lbl]} @state/control-instances
        mission-path (ui/get-text single-path-fld)]
    (when (not (string/blank? mission-path))
      (ui/set-label single-path-lbl mission-path))))

(defn single-choose-command
  "### single-choose-command
   This zero argument function gets the mission file selected by the user
   after displaying a mission file chooser. This value is relativised
   against the Missions directory so that it is in the format expected
   by the server in LOAD commands.

   This value is loaded into the single mission path label as the active
   mission to load."
  []
  (let [{:keys [mis-chooser
                single-path-lbl]} @state/control-instances
        file (ui/show-chooser mis-chooser)]
    (when file
      (ui/set-label single-path-lbl
                    (get-relative-path @state/server-path (.getCanonicalPath file))))))

(defn single-path-select
  "### single-path-select
   This zero argument function sets the global single mission path atom.
   It is called when the single mission path label text changes."
  []
  (let [{:keys [single-path-lbl]} @state/control-instances
        single-path (ui/get-text single-path-lbl)]
    (if (= single-path "...")
      (reset! state/single-mission-path nil)
      (reset! state/single-mission-path single-path))))