;; ## Parser definitions
;;
;; This namespace defines the parsers that we use to parse the server console output
;; on our tapped channels.
(ns uk.org.il2ssd.parse
  (:require [instaparse.core :as insta]))

(def difficulty-parser
  "### difficulty-parser
   This parser returns a vector in the format:

       [:difficulty-rec [:setting setting] [:value value]]

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

       [:line [:path path] [:mission mission] [:state state]]

   for each line passed into the parser. This is used to extract the mission state from
   the server console output, and the mission and path if these are available."
  (insta/parser
    "line = <'Mission'> (<':'> <sp> path+ mission <sp> <'is'>)? <sp> state <nl>
     <sp> = #'\\s'
     <nl> = '\n'
     path = #'.+/'
     mission = #'.+\\.mis'
     state = 'Playing' | 'Loaded' | 'NOT loaded'"))

(defn parse-text
  "### parse-text
   This two argument function passes the input text through the input parser.
   After receiving this text, the output is stripped of the redundant root element,
   flattened into a sequence of key-value pairs, then loaded into a map for easy
   keyed access.

   For example, the mission state map returned by the mission parser:

       [:line [:path path] [:mission mission] [:state state]]

   ...will be transformed into this:

       {:path path, :mission mission, :state state}"
  [parser text]
  (->> text
       parser
       rest
       flatten
       (apply hash-map)))
