;;;;
;;;; JavaFX helper functions; most shamelessly ripped from Upshot
;;;;
(ns uk.org.il2ssd.jfx)

(defn run-later*
    [f]
    (javafx.application.Platform/runLater f))

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
    (reify javafx.event.EventHandler
        (handle [this e] (f e))))

(defmacro event-handler [arg & body]
    `(event-handler* (fn ~arg ~@body)))

(defn invalidation-listener*
    [f]
    (reify javafx.beans.InvalidationListener
        (invalidated [this observable] (f observable))))

(defmacro invalidation-listener [arg & body]
    `(invalidation-listener* (fn ~arg ~@body)))

(defn change-listener*
    [f]
    (reify javafx.beans.value.ChangeListener
        (changed [this observable old new]
            (f observable old new))))

(defmacro change-listener [arg & body]
    `(change-listener* (fn ~arg ~@body)))