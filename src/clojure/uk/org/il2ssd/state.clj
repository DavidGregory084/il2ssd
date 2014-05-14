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

(def params
  "### params
   This atom will store the application parameters for testing purposes."
  (atom nil))

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
   by the afterburner.fx Presenter class for each .fxml file, along with sets of
   enablement and disablement dependencies for each object."
  (atom nil))

(def control-instances
  "### control-instances
   This atom will store a map of the control object instances which are returned
   by the afterburner.fx Presenter class for each .fxml file, mapped directly to
   the top=level keys."
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

(def loading
  "### loading
   This atom stores a true or false loading status for global use in the program."
  (atom nil))

(def cycle-running
  "### cycle-running
   This atom stores a true or false cycle running state for global use in the
   program."
  (atom nil))

(def dcg-running
  "### dcg-running
   This atom stores a true or false DCG running state for global use in the
   program."
  (atom nil))

(def server-path
  "### server-path
   This atom stores the server path for global use in the program."
  (atom nil))

(def dcg-path
  "### dcg-path
   This atom stores the DCG executable path for global use in the
   program."
  (atom nil))

(def single-mission-path
  "### mission-path
   This atom stores the current mission path for global use in the
   program. It is also used to indicate whether a mission can be
   loaded."
  (atom nil))

(def cycle-mission-path
  "### cycle-mission-path
   This atom stores the current cycle mission path. It is also used
   to indicate whether a cycle can be started."
  (atom nil))

(def dcg-mission-path
  "### dcg-mission-path
   This atom stores the current DCG mission path. It is also used to
   indicate whether a DCG mission can be loaded."
  (atom nil))

(def mode
  "### mode
   This atom stores the current mission loading mode for global use
   in the program."
  (atom nil))

(def cycle-index
  "### cycle-index
   This atom stores the current cycle index for global use in the
   program."
  (atom 0))

(defn get-state
  "### get-state
   This function returns a map containing the current state of all
   of the global state atoms which are relevant to the UI state,
   keyed by the name of the atom."
  []
  {:connected           @connected
   :loaded              @loaded
   :loading             @loading
   :playing             @playing
   :server-path         @server-path
   :dcg-path            @dcg-path
   :single-mission-path @single-mission-path
   :cycle-mission-path  @cycle-mission-path
   :dcg-mission-path    @dcg-mission-path
   :cycle-running       @cycle-running
   :cycle-index         @cycle-index
   :dcg-running         @dcg-running})