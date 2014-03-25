;;
;; ## Global state atoms
;;
;; In this namespace we define a number of atoms which will simply hold true or
;; false values that will define the global state of the program.
;;
;; We can add watches to these atoms to define an on-changed function just as we
;; might add listeners in a Java UI toolkit.
;;
;; Because Clojure comparison functions treat nil as logical false, it is not
;; necessary to initialise these atoms.
(ns uk.org.il2ssd.state)

(def stage
  "### stage
   This atom will store the JavaFX Stage instance so that we can set the stage
   title during the execution of the program and return the instance for testing
   using the TestFX library."
  (atom nil))

(def presenters
  "### presenters
   This atom will store a map of presenter instances."
  (atom nil))

(def controls
  "### controls
   This atom will store a map of the control object instances which are returned
   by the afterburner.fx Presenter class for each .fxml file."
  (atom nil))

(def connected
  "### connected
   This atom simply stores a true or false connected status for global use in the
   program."
  (atom nil))

(def loaded
  "### loaded
   This atom simply stores a true or false mission loaded status for global use
   in the program."
  (atom nil))

(def playing
  "### playing
   This atom stores a true or false mission playing status for global use in the
   program."
  (atom nil))

(def mis-selected
  "### mis-selected
   This atom stores a true or false mission selected status for global use in the
   program."
  (atom nil))

(def cycle-running
  "### cycle-running
   This atom stores a true or false cycle running state for global use in the
   program."
  (atom nil))

(def server-path
  "### server-path
   This atom stores the server path for global use in the program."
  (atom nil))

(def mission-path
  "### mission-path
   This atom stores the current mission path for global use in the
   program."
  (atom nil))

(def mode
  "### mode
   This atom stores the current mission loading mode for global use
   in the program."
  (atom nil))