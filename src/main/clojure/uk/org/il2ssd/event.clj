;;;;
;;;; Input event handling
;;;;
(ns uk.org.il2ssd.event
    (:require [uk.org.il2ssd.jfx :as jfx])
    (:import (javafx.application Platform)
             (javafx.stage WindowEvent)
             (javafx.scene.input KeyEvent)))

(defn nothing []
    (jfx/event-handler [_] ()))

(defn close []
    (jfx/event-handler [windowevent] (do (.consume windowevent)
                                         (println "Exiting...")
                                         (Platform/exit))))

(defn enter-command []
    (jfx/event-handler [keyevent] (if (= (-> keyevent (.getCode) (.getName)) "Enter")
                                      (println "Enter"))))