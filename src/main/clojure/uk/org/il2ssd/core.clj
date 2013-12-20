;;;;
;;;; Main application class
;;;;
(ns uk.org.il2ssd.core
    (:gen-class :name uk.org.il2ssd.core
                :extends javafx.application.Application
                :main true)

    (:require [uk.org.il2ssd.ui :as ui])

    (:import (javafx.application Application)
             (com.airhacks.afterburner.injection InjectionProvider)))

(defn -main [& args]
    (Application/launch uk.org.il2ssd.core (into-array String [args])))

(defn -start [this primaryStage]
    (-> primaryStage
        (ui/init-stage)
        (ui/init-objects)
        (ui/init-handlers)
        (println)))

(defn -stop [& args]
    (InjectionProvider/forgetAll))

