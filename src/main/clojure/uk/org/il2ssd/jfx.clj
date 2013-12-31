;;;;
;;;; JavaFX helper functions; most shamelessly ripped from Upshot
;;;;
(ns uk.org.il2ssd.jfx
  (:import (javafx.application Platform)
           (javafx.event EventHandler)
           (javafx.beans InvalidationListener)
           (javafx.beans.value ChangeListener)))

(defn run-later*
  [f]
  (Platform/runLater f))

(defmacro run-later
  [& body]
  `(run-later* (fn [] ~@body)))

(defn run-now*
  [f]
  (let [result (promise)]
    (run-later
      (deliver result (try (f) (catch Throwable e e))))
    @result))

(defmacro run-now
  [& body]
  `(run-now* (fn [] ~@body)))

(defn event-handler*
  [f]
  (reify EventHandler
    (handle [this e] (f e))))

(defmacro event-handler [arg & body]
  `(event-handler* (fn ~arg ~@body)))

(defn invalidation-listener*
  [f]
  (reify InvalidationListener
    (invalidated [this observable] (f observable))))

(defmacro invalidation-listener [arg & body]
  `(invalidation-listener* (fn ~arg ~@body)))

(defn change-listener*
  [f]
  (reify ChangeListener
    (changed [this observable old new]
      (f observable old new))))

(defmacro change-listener [arg & body]
  `(change-listener* (fn ~arg ~@body)))