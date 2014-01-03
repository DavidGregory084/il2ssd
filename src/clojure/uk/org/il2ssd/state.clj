;;;;
;;;; Global state
;;;;
(ns uk.org.il2ssd.state)

(def stage
  "Atom to store the stage instance."
  (atom nil))
(def controls
  "Atom to store the map of instantiated controls from FXML."
  (atom nil))
(def connected
  "Atom to store connection status."
  (atom nil))
(def loaded
  "Atom to store mission loaded status."
  (atom nil))
(def playing
  "Atom to store mission playing status."
  (atom nil))
(def mis-selected
  "Atom to store mission selected status."
  (atom nil))
