;; ## Parser definitions
;;
;; This namespace defines the parsers that we use to parse the server console output
;; on our tapped channels.
(ns uk.org.il2ssd.parse
  (:require [instaparse.core :as insta]))

(def difficulty-parser
  "### difficulty-parser
   This parser returns a vector in the format:

       [:difficulty-rec [:setting setting :value value]]

   for each line passed into the parser. This is used to extract the difficulty settings from
   the server console output."
  (insta/parser
    "difficulty-rec = <2sp> setting <sp> value <nl>
    <sp> = #'\\s*+'
    <2sp> = #'\\s{2}'
    <nl> = '\n'
    setting = #'[A-Z[a-z[_]]]+'
    value = #'[0-1]'"))

(def mission-parser
  "### mission-parser
   This parser returns a vector in the format:

       [:line [:state state]]

   or:

       [:line [:path path :mission mission :state state]]

   for each line passed into the parser. This is used to extract the mission state from
   the server console output, and the mission and path if these are available."
  (insta/parser
    "line = <'Mission'> (<':'> <sp> path+ mission <sp> <'is'>)? <sp> state <nl>
     <sp> = #'\\s'
     <nl> = '\n'
     path = #'.+/'
     mission = #'.+\\.mis'
     state = 'Playing' | 'Loaded' | 'NOT loaded'"))