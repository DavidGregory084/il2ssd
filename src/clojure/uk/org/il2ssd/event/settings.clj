(ns uk.org.il2ssd.event.settings
  (:require [uk.org.il2ssd.jfx.ui :as ui]
            [uk.org.il2ssd.server :as server]
            [uk.org.il2ssd.state :as state])
  (:import (java.io File)))

(defn server-choose-command
  "### server-choose-command
   This zero argument function displays the server chooser dialog and uses
   the provided file to set the server path in the UI."
  []
  (let [{:keys [server-chooser
                server-path-lbl]} @state/control-instances
        file (ui/show-chooser server-chooser)]
    (when file
      (ui/set-label server-path-lbl (.getCanonicalPath file)))))

(defn server-path-select
  "### server-path-select
   This zero argument function sets the global server path atom. It is
   called when the server path label text changes."
  []
  (let [{:keys [server-path-lbl]} @state/control-instances
        server-path (ui/get-text server-path-lbl)]
    (if (= server-path "...")
      (reset! state/server-path nil)
      (reset! state/server-path server-path))))

(defn get-difficulties
  "### get-difficulties
   This is a zero argument function which clears the difficulty data list and
   requests the currently loaded difficulty settings from the server so that they
   can be parsed back into the program by the listener function."
  []
  (ui/clear-diff-data @state/control-instances)
  (server/get-difficulty))

(defn set-difficulties
  "### set-difficulties
   This is a zero argument function which iterates over the difficulty data list
   and gets the setting and value for each difficulty setting in the list,
   setting each provided setting to the provided value on the server.

   This will update the server with any changes that the user has made to the
   difficulty settings list."
  []
  (let [{:keys [diff-data]} @state/control-instances]
    (doseq [item diff-data]
      (let [item-data (ui/get-item-data item)
            setting (:setting item-data)
            value (:value item-data)]
        (server/set-difficulty setting value)))))