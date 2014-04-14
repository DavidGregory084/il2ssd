;;
;; ## JavaFX helper functions
;;
;; These functions wrap common JavaFX methods or return instances of anonymous classes
;; which override their methods.
;; Most are shamelessly stolen from the now-abandoned Upshot library which was begun by
;; Dave Ray, originator of the excellent Seesaw, a Clojure wrapper for the Swing toolkit.
(ns uk.org.il2ssd.jfx.util
  (:import (javafx.application Platform)
           (javafx.event EventHandler Event)
           (javafx.beans InvalidationListener)
           (javafx.beans.value ChangeListener ObservableValue)
           (javafx.scene.control ButtonBase ChoiceBox Labeled)
           (javafx.scene.input KeyEvent)
           (javafx.scene Node)
           (javafx.stage Stage)))

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
  ^EventHandler [f]
  (reify EventHandler
    (handle [this e] (f e))))

(defmacro event-handler
  "### event-handler
   This macro expands the contents of the argument vector and function body into a new
   anonymous function which is passed into the event-handler* function above."
  ^EventHandler [arg & body]
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
  ^InvalidationListener [arg & body]
  `(invalidation-listener* (fn ~arg ~@body)))

(defn change-listener*
  "### change-listener*
   This function accepts a function f, and returns an instance of an anonymous class
   which overrides the changed method of javafx.beans.value.ChangeListener using f.

   This allows listeners to be defined for JavaFX ObservableValues which trigger
   Clojure functions when the ObservableValue is changed.

   ChangeListeners use eager evaluation, so the old and new value of the
   ObservableValue can be used in the function."
  ^ChangeListener [f]
  (reify ChangeListener
    (changed [this observable old new]
      (f observable old new))))

(defmacro change-listener
  "### change-listener
   This macro expands the contents of the argument vector and function body into a new
   anonymous function which is passed into the change-listener* function above."
  ^ChangeListener [arg & body]
  `(change-listener* (fn ~arg ~@body)))

(defn key-pressed?
  "### key-pressed?
   This two argument function returns true if the supplied KeyEvent keycode name
   matches the key defined in the supplied string."
  [^KeyEvent event argkey]
  (let [actualkey (-> event .getCode .getName)]
    (if (= actualkey argkey)
      true)))

(defn button-handler
  "### button-handler
   This two argument function attaches an EventHandler instance to the supplied
   control which calls the supplied function when the OnAction event is triggered."
  [control f]
  (.setOnAction control (event-handler [_] (f))))

(defn close-handler
  [control f]
  (.setOnCloseRequest control (event-handler [_] (f))))

(defn keypress-handler
  "### keypress-handler
   This three argument function attaches an EventHandler instance to the supplied
   control which calls the supplied function if the named keyboard key is pressed
   in the control's context."
  [^Node control keyname f]
  (.setOnKeyPressed control (event-handler [keyevent]
                                           (when
                                               (key-pressed? keyevent keyname)
                                             (f)))))

(defn value-listener
  "### value-listener
   This three argument function attaches an InvalidationListener instance to the
   supplied control's valueProperty, which calls the supplied function with the
   supplied arguments when the value is changed."
  ([^ChoiceBox control f]
   (-> control
       ^ObservableValue .valueProperty
       (.addListener (invalidation-listener [_] (f)))))
  ([^ChoiceBox control f arg]
   (-> control
       ^ObservableValue .valueProperty
       (.addListener (invalidation-listener [_] (f arg))))))

(defn text-listener
  "### text-listener
   This three argument function attaches an InvalidationListener instance to the
   supplied control's textProperty, which calls the supplied function with the
   supplied arguments when the text is changed."
  ([^Labeled control f]
   (-> control
       .textProperty
       (.addListener (invalidation-listener [_] (f)))))
  ([^Labeled control f arg]
   (-> control
       .textProperty
       (.addListener (invalidation-listener [_] (f arg))))))

(defn focus-listener
  "### focus-listener
   This two argument function attaches a ChangeListener instance to the supplied
   control's focusedProperty, which calls the supplied function with the new value
   of the focusedProperty as an argument."
  [^Node control f]
  (-> control
      .focusedProperty
      (.addListener (change-listener [_ _ newval] (f newval)))))