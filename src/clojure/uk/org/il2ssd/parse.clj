;; ## Parser definitions
;;
;; This namespace defines the parsers that we use to parse the server console output
;; on our tapped channels.
(ns uk.org.il2ssd.parse
  (:require [instaparse.core :as insta]))

(def difficulty-parser
  "### difficulty-parser
   This parser returns a vector in the format:

       [:line [:setting setting] [:value value]]

   for each line passed into the parser. This is used to extract the difficulty settings from
   the server console output."
  (insta/parser
    "line = <2sp> setting <sp> value <nl>
    <sp> = #'\\s*+'
    <2sp> = #'\\s{2}+'
    <nl> = '\\n'
    setting = #'\\w+(?=\\s*\\d)'
    value = #'[0-1]{1}'"))

(def mission-parser
  "### mission-parser
   This parser returns a vector in the format:

       [:line [:state state]]

   or:

       [:line [:path path] [:mission mission] [:state state]]

   for each line passed into the parser. This is used to extract the mission state from
   the server console output, and the mission and path if these are available."
  (insta/parser
    "line = <'Mission'> (<': '> path+ mission <' is'>)? <sp> state <nl>
     <sp> = #'\\s{1}+'
     <nl> = '\\n'
     path = #'.+/'
     mission = #'.+\\.mis'
     state = 'Playing' | 'Loaded' | 'NOT loaded'"))

(def pilot-parser
  "### pilot-parser"
  (insta/parser
    "line = <begin> join? ip port name? leave?
     <begin> = 'socket channel ' | 'socketConnection with '
     socket =  #'\\d++'
     <join> = (<'\\''> socket <'\\''> <', ip '>)
     ip = #'(\\d{1,3}+\\.?{1}+){4}+'
     port = <':'> #'\\d++'
     name = <', '> #'.+(?=, is complete created\\n)' <', is complete created\\n'>
     <leave> = <' on channel '> socket <' lost.  Reason:'> <#'.*\\n'>"))

(def user-parser
  (insta/parser
    "line = <sp> number name ping score army aircraft? <nl>
     <sp> = #'\\s'
     <nsp> = #'\\s++'
     <nl> = #'\\s*\\n'
     number = #'\\d++' <nsp>
     name = #'.+?(?=\\s++\\d++\\s++\\d++\\s++\\(\\d)' <nsp>
     ping = #'\\d++' <nsp>
     score = #'\\d++' <nsp>
     <army> = <'('> armyno <')'> armyname
     armyno = #'\\d++'
     armyname = #'\\w++'
     aircraft = <nsp> #'\\w.+(?=\\n)'"))

(def host-parser
  (insta/parser
    "line = <sp> number <sp> name <sp> socket ip port <nl>
     <sp> = #'\\s'
     <nl> = '\\n'
     number = #'\\d++' <':'>
     name = #'.+?(?=\\s\\[\\d++\\](\\d{1,3}+\\.?{1}+){4}+)'
     socket = <'['> #'\\d++' <']'>
     ip = #'(\\d{1,3}+\\.?{1}+){4}+'
     port = <':'> #'\\d++'"))

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
  (let [result (parser text)]
    (if (insta/failure? result)
      (println result)
      (->> result
           rest
           flatten
           (apply hash-map)))))
