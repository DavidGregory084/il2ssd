;;
;; ## Main application class and main method
;;
;; This namespace must be AOT compiled for the main application class to be generated!
;;
;; Here we call all of our main initialisation methods after launching the application
;; on the JavaFX Application thread.
(ns uk.org.il2ssd.core
  (:gen-class :name uk.org.il2ssd.core
              :extends javafx.application.Application
              :main true)

  (:require [uk.org.il2ssd.ui-init :as ui])

  (:import (javafx.application Application)
           (com.airhacks.afterburner.injection InjectionProvider)
           (uk.org.il2ssd core)))

(defn -main
  "### -main
   This is the main application method. It launches the main method of the class we
   defined above with gen-class using the JavaFX Application.launch static method.

   This class will be generated when this namespace is AOT compiled."
  [& args]
  (Application/launch core (into-array String [args])))

(defn -start
  "### -start
   Every JavaFX application runs the \"start\" method immediately after launching on
   the JavaFX Application Thread.

   We use this method to launch all of our UI initialisation methods, first initialising
   the stage and then passing the instance of our MainPresenter class into our object
   initialisation method.

   After initialising the objects, we initialise event handlers, watches and controls."
  [this primaryStage]
  (let [main-presenter (ui/init-stage primaryStage)]
    (ui/init-objects main-presenter)
    (ui/init-handlers)
    (ui/init-controls)
    (ui/init-choosers)))

(defn -stop
  "### -stop
   Every JavaFX application runs the \"stop\" method when a close request is made for
   the last visible stage, so that cleanup can be performed before the application
   is closed.

   At present we only call the forgetAll method for the afterburner.fx dependency
   injection framework's InjectionProvider."
  [& args]
  (InjectionProvider/forgetAll))

