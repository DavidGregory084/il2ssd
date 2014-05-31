(ns uk.org.il2ssd.event.dcg
  (:require [clojure.core.async :refer [go]]
            [clojure.string :as string]
            [uk.org.il2ssd.state :as state]
            [uk.org.il2ssd.jfx.ui :as ui]
            [uk.org.il2ssd.server :as server]
            [uk.org.il2ssd.event.cycle :as cycle]
            [clj-commons-exec :as exec]
            [uk.org.il2ssd.event.scheduler :as schedule])
  (:import (java.io BufferedReader File)
           (java.nio.file Files Path Paths LinkOption)
           (java.nio.charset Charset)))

(declare schedule-next-mis)

(defn toggle-timer
  []
  (let [{:keys [dcg-timer-toggle]} @state/control-instances
        enabled (ui/get-toggle-selected dcg-timer-toggle)]
    (if enabled
      (reset! state/dcg-timer true)
      (do (reset! state/dcg-timer false)
          (when @state/dcg-running
            (schedule/stop-scheduled-mis)
            (schedule/reset-schedule))))))

(defn get-generated-mis
  []
  (let [dcg-exe (Paths/get @state/dcg-path (into-array String []))
        dcg-dir (.getParent dcg-exe)
        mis-list (.resolve dcg-dir "missions.txt")]
    (when (Files/exists mis-list (into-array LinkOption []))
      (let [reader (Files/newBufferedReader
                     mis-list (Charset/defaultCharset))
            dcg-mis-path (str "Net/" (.readLine reader))]
        (string/replace dcg-mis-path "\\" "/")))))

(defn dcg-choose-command
  "### dcg-choose-command
   This zero argument function displays the DCG chooser dialog and uses
   the provided file to set the DCG executable path in the UI."
  []
  (let [{:keys [dcg-chooser
                dcg-path-lbl]} @state/control-instances
        file (ui/show-chooser dcg-chooser)]
    (when file
      (ui/set-label dcg-path-lbl (.getCanonicalPath file)))))

(defn dcg-path-select
  "### dcg-path-select
   This zero argument function sets the global DCG executable path atom.
   It is called when the DCG path label text changes."
  []
  (let [{:keys [dcg-path-lbl
                dcg-mis-lbl]} @state/control-instances
        dcg-path (ui/get-text dcg-path-lbl)]
    (if (= dcg-path "...")
      (do (reset! state/dcg-path nil)
          (ui/set-label dcg-mis-lbl "..."))
      (do (reset! state/dcg-path dcg-path)
          (let [dcg-mis-path (get-generated-mis)]
            (when-not (string/blank? dcg-mis-path)
              (ui/set-label dcg-mis-lbl dcg-mis-path)))))))

(defn generate-dcg-mis
  [scheduled]
  (when-not scheduled
    (schedule/stop-scheduled-mis))
  (let [dcg-gen (exec/sh [@state/dcg-path "/netdogfight"])]
    (reset! state/loading true)
    (go (println @dcg-gen)
        (let [{:keys [dcg-mis-lbl]} @state/control-instances
              dcg-mis-path (get-generated-mis)]
          (when-not (string/blank? dcg-mis-path)
            (ui/set-label dcg-mis-lbl dcg-mis-path)
            (server/load-begin-mission dcg-mis-path)
            (schedule-next-mis))))))

(defn schedule-next-mis
  []
  (when @state/dcg-timer
    (let [{:keys [dcg-timer-fld]} @state/control-instances
          timer (ui/get-text dcg-timer-fld)]
      (schedule/schedule-mission #(generate-dcg-mis true) timer))))

(defn dcg-mis-generated
  []
  (let [{:keys [dcg-mis-lbl]} @state/control-instances
        dcg-mis-path (ui/get-text dcg-mis-lbl)]
    (if (= dcg-mis-path "...")
      (reset! state/dcg-mission-path nil)
      (reset! state/dcg-mission-path dcg-mis-path))))

(defn start-stop-dcg-command
  []
  (let [{:keys [dcg-mis-lbl]} @state/control-instances
        dcg-mis-path (ui/get-text dcg-mis-lbl)]
    (if @state/dcg-running
      (do (schedule/stop-scheduled-mis)
          (schedule/reset-schedule)
          (server/unload-mission))
      (do (when @state/cycle-running
            (cycle/stop-cycle))
          (reset! state/dcg-running true)
          (if (= dcg-mis-path "...")
            (generate-dcg-mis true)
            (do (reset! state/loading true)
                (server/load-begin-mission dcg-mis-path)
                (schedule-next-mis)))))))
