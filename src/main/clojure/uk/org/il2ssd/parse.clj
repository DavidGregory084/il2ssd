;;;;
;;;; Parsing functions
;;;;
(ns uk.org.il2ssd.parse
    (:require [instaparse.core :as insta]))

(def difficulty-parser
    (insta/parser
        "<difficulty-rec> = <sp>* setting <sp> number <nl>?
        <sp> = #'\\s+'
        <nl> = #'[\\s*\n]+'
        setting = #'\\w+'
        number = #'[0-1]'"))

(def file-parser
    (insta/parser
        "file = <header> <nl>+ section+
         <sp> = #'\\s+'
         <nl> = #'[\\s*\n]+'
         <header> = #'\\[.+\\]'
         <section> = mission | server
         mission = <#'\\[Mission\\]'> (<nl> setting)*
         server = <#'\\[Server\\]'> (<nl> setting)*
         setting = key <equals> value? <nl>*
         <key> = #'\\w+'
         <value> = #'[\\w[\\.]]+'
         <equals> = #'\\s?=\\s?'"))

(def mission-parser
    (insta/parser
        "<line> = <'Mission'> (<':'> <sp> path mission <sp> <'is'>)? <sp> state <nl>?
         <sp> = <#'\\s+'>
         <nl> = #'[\\s*\n]+'
         path = #'.+/+'
         mission = #'.+\\.mis\\b'
         state = 'Playing' | 'Loaded' | 'NOT loaded'"))