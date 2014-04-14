;;
;; ## Main application class and main method
;;
;; This namespace must be AOT compiled for the main application class to be generated!
;;
;; Here we call all of our main initialisation methods after launching the application
;; on the JavaFX Application thread.
(ns uk.org.il2ssd.core
  (:require [uk.org.il2ssd.jfx.init :as jfx]
            [uk.org.il2ssd.state :as state])
  (:import (com.airhacks.afterburner.injection InjectionProvider)
           (javafx.application Application Application$Parameters)
           (uk.org.il2ssd Core))
  (:gen-class :name uk.org.il2ssd.Core
              :extends javafx.application.Application
              :main true
              :methods [#^{:static true} [getStage [] javafx.stage.Stage]]))

(defn -getStage
  "### -getStage
   This zero-argument function appears externally as a static method which returns
   the javafx.stage.Stage instance for our current stage.

   This was done as a requirement for using the TestFX framework, which requires
   access to the root node of the scene when extending the getRootNode method
   of the GuiTest class."
  []
  @state/stage)

(defn -main
  "### -main
   This is the main application method, which calls the main method of Core.class
   which we defined above with gen-class, using the JavaFX Application.launch
   static method.

   The file Core.class will be generated when this namespace is AOT compiled."
  [& args]
  (Application/launch Core (into-array String args)))

(defn -start
  "### -start
   Every JavaFX application runs the \"start\" method immediately after launching on
   the JavaFX Application Thread.

   We use this method to launch all of our UI initialisation methods, first initialising
   the stage and then our object map, which will hold each of the controls we need to
   manipulate.

   After initialising the objects map, we initialise event handlers, watches and the
   state for our standard controls from defaults or from our stored config file.

   Finally the tables and file choosers are instantiated, which requires some extra
   configuration."
  [^Core this stage]
  (reset! state/params (-> this .getParameters .getRaw))
  (reset! state/stage stage)
  (jfx/init-stage)
  (jfx/init-objects)
  (jfx/init-handlers)
  (jfx/init-controls)
  (jfx/init-choosers)
  (jfx/init-diff-table)
  (jfx/init-cycle-table))

(defn -stop
  "### -stop
   Every JavaFX application runs the \"stop\" method when a close request is made for
   the last visible stage, so that cleanup can be performed before the application
   is closed.

   At present we only call the forgetAll method for the afterburner.fx dependency
   injection framework's InjectionProvider."
  [this]
  (InjectionProvider/forgetAll))

