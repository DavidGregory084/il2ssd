;;;;
;;;; Main application class
;;;;
(ns uk.org.il2ssd.core
    (:gen-class :name uk.org.il2ssd.core
                :extends javafx.application.Application
                :main true)

    (:require [uk.org.il2ssd.ui-init :as ui])

    (:import (javafx.application Application)
             (com.airhacks.afterburner.injection InjectionProvider)
             (uk.org.il2ssd core)))

(set! *warn-on-reflection* true)

(defn -main [& args]
    (Application/launch core (into-array String [args])))

(defn -start [this primaryStage]
    ((-> (ui/init-stage primaryStage) (ui/init-objects))
        (ui/init-handlers)
        (ui/init-controls)))

(defn -stop [& args]
    (InjectionProvider/forgetAll))

