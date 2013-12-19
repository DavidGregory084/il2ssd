;;;;
;;;; Main application class
;;;;
(ns uk.org.il2ssd.core
    (:gen-class :name uk.org.il2ssd.core
                :extends javafx.application.Application
                :main true)

    (:require [uk.org.il2ssd.jfx :as jfx])

    (:import (javafx.application Application Platform)
             (javafx.scene Scene)
             (javafx.scene.text Font)
             (javafx.stage Stage WindowEvent)
             (com.airhacks.afterburner.injection InjectionProvider)
             (uk.org.il2ssd MainView MainPresenter)))

(defn -main [& args]
    (Application/launch uk.org.il2ssd.core (into-array String [args])))

(defn -start [this primaryStage]
    (let [scene (Scene. (.getView (MainView.)))]
        (Font/loadFont "fontawesome-webfont.ttf" 12.0)
        (doto primaryStage
            (.setTitle "Il-2 Simple Server Daemon")
            (.setScene scene)
            (.show)
            (.setOnCloseRequest
                (jfx/event-handler [_] (Platform/exit))))))

(defn -stop [& args]
    (InjectionProvider/forgetAll))

