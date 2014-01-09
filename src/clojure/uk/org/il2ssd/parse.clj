;; ## Parser definitions
;;
;; This namespace defines the parsers that we use to parse the server console output
;; on our tapped channels.
(ns uk.org.il2ssd.parse
  (:require [instaparse.core :as insta]))

(def difficulty-parser
  "### difficulty-parser
   This parser returns a vector in the format

       [:setting setting :value value]

   for each line passed into the parser. This is used to extract the difficulty settings from
   the server console output."
  (insta/parser
    "<difficulty-rec> = <sp>* setting <sp> number <nl>?
    <sp> = #'\\s+'
    <nl> = #'[\\s*\n]+'
    setting = #'\\w+'
    number = #'[0-1]'"))

(def mission-parser
  "### mission-parser
   This parser returns a vector in the format

       [:path path :mission mission :state state]

   for each line passed into the parser. This is used to extract the mission state from
   the server console output."
  (insta/parser
    "<line> = <'Mission'> (<':'> <sp> path mission <sp> <'is'>)? <sp> state <nl>?
     <sp> = <#'\\s+'>
     <nl> = #'[\\s*\n]+'
     path = #'.+/+'
     mission = #'.+\\.mis\\b'
     state = 'Playing' | 'Loaded' | 'NOT loaded'"))