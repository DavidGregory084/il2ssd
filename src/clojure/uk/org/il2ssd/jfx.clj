;;
;; ## JavaFX helper functions
;;
;; These functions wrap common JavaFX methods or return instances of anonymous classes
;; which override their methods.
;; Most are shamelessly stolen from the now-abandoned Upshot library which was begun by
;; the creator of Seesaw, a Clojure wrapper for the Swing toolkit.
(ns uk.org.il2ssd.jfx
  (:import (javafx.application Platform)
           (javafx.event EventHandler)
           (javafx.beans InvalidationListener)
           (javafx.beans.value ChangeListener)))

(defn run-later*
  "### run-later*
   This function accepts a function f, and passes this function to  the static JavaFX
   Platform.runLater method, which runs the specified task on the JavaFX Application
   Thread.

   This is possible because Clojure functions implement the Runnable and Callable
   interfaces.

   This function returns immediately, and is necessary to update the UI from a
   different thread."
  [f]
  (Platform/runLater f))

(defmacro run-later
  "### run-later
   This macro expands the contents of the argument into a zero-argument function
   which is passed into the run-later* function above."
  [& body]
  `(run-later* (fn [] ~@body)))

(defn run-now*
  "### run-now*
   This one argument function attempts to run function f on the JavaFX
   Application Thread, catching any exception which may result and capturing
   the result in a promise, the dereferenced value of which is returned to
   the calling function.

   Because this function attempts to evaluate the result of calling function
   f, which Platform.runLater will execute at an arbitrary time on the JavaFX
   Application Thread, this function will block until function f returns.

   This can be used to block execution until some change has been made to the
   user interface."
  [f]
  (let [result (promise)]
    (run-later
      (deliver result (try (f) (catch Throwable e e))))
    @result))

(defmacro run-now
  "### run-now
   This macro expands the contents of the argument into a zero-argument function
   which is passed into the run-now* function above."
  [& body]
  `(run-now* (fn [] ~@body)))

(defn event-handler*
  "### event-handler*
   This function accepts a function f, and returns an instance of an anonymous class
   which overrides the handle method of javafx.event.EventHandler using f.
   This allows event handlers to be defined for JavaFX controls as Clojure functions."
  [f]
  (reify EventHandler
    (handle [this e] (f e))))

(defmacro event-handler
  "### event-handler
   This macro expands the contents of the argument vector and function body into a new
   anonymous function which is passed into the event-handler* function above."
  [arg & body]
  `(event-handler* (fn ~arg ~@body)))

(defn invalidation-listener*
  "### invalidation-listener*
   This function accepts a function f, and returns an instance of an anonymous class
   which overrides the invalidated method of javafx.beans.InvalidationListener using f.

   This allows listeners to be defined for JavaFX ObservableValues which trigger
   Clojure functions when the ObservableValue is changed.

   InvalidationListeners do not pass the new value into the function so that the new
   value doesn't have to be evaluated."
  [f]
  (reify InvalidationListener
    (invalidated [this observable] (f observable))))

(defmacro invalidation-listener
  "### invalidation-listener
   This macro expands the contents of the argument vector and function body into a new
   anonymous function which is passed into the invalidation-listener* function above."
  [arg & body]
  `(invalidation-listener* (fn ~arg ~@body)))

(defn change-listener*
  "### change-listener*
   This function accepts a function f, and returns an instance of an anonymous class
   which overrides the changed method of javafx.beans.value.ChangeListener using f.

   This allows listeners to be defined for JavaFX ObservableValues which trigger
   Clojure functions when the ObservableValue is changed.

   ChangeListeners use eager evaluation, so the old and new value of the
   ObservableValue can be used in the function."
  [f]
  (reify ChangeListener
    (changed [this observable old new]
      (f observable old new))))

(defmacro change-listener
  "### change-listener
   This macro expands the contents of the argument vector and function body into a new
   anonymous function which is passed into the change-listener* function above."
  [arg & body]
  `(change-listener* (fn ~arg ~@body)))