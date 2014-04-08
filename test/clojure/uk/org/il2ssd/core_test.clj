(ns uk.org.il2ssd.core_test
  (:import (javafx.stage Stage)
           (javafx.application Platform Application Application$Parameters))
  (:require [uk.org.il2ssd.core :refer :all]
            [uk.org.il2ssd.state :as state]
            [uk.org.il2ssd.jfx.util :as util]
            [clojure.test :refer :all]
            [clojure.core.async :refer [go]]))

(defn main-fixture
  "Launches the application on the JavaFX Application Thread before testing."
  [f]
  (go (-main (into-array String ["args" "go" "here"])))
  (while (nil? @state/stage)
    nil)
  (f)
  (util/run-later (Platform/exit)))

(use-fixtures :once main-fixture)

(deftest getStage
  (testing "Getting a stage instance"
    (testing "from the static method"
      (is (instance? Stage (-getStage))))
    (testing "from the state atom"
      (is (instance? Stage @state/stage)))))

(deftest getParameters
  (testing "Getting parameters"
    (is (= @state/params '("args" "go" "here")))))