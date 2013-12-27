;;;;
;;;; Global state
;;;;
(ns uk.org.il2ssd.state)

(def title (atom nil))
(def stage (atom nil))
(def controls (atom nil))
(def connected (atom nil))
(def mode (atom nil))
(def loaded (atom nil))
(def playing (atom nil))
(def modes {:single "Single Mission", :cycle "Mission Cycle", :dcg "DCG Generation"})
