;;;;
;;;; Parsing functions
;;;;
(ns uk.org.il2ssd.parse
    (:require [uk.org.il2ssd.channel :refer :all]
              [uk.org.il2ssd.state :as state]
              [uk.org.il2ssd.settings :as settings]
              [clojure.core.async :refer [thread <!!]]
              [instaparse.core :as insta]))

(def difficulty-parser
    (insta/parser
        "<difficulty-rec> = <sp>* setting <sp> number <nl>
        <sp> = <#'\\s+'>
        <nl> = <#'[\\s*\n]+'>
        setting = #'\\w+'
        number = #'[0-1]'"))

(defn parse-difficulty []
    (thread (while @state/connected
                (let [text (<!! parse-channel)]
                    (let [parsed (difficulty-parser text)
                          [[_ setting] [_ number]] parsed]
                        (swap! settings/difficulties assoc setting number))))))