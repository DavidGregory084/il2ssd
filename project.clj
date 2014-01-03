(defproject uk.org.il2ssd/il2ssd "0.1.0-SNAPSHOT"
  :description "Il-2 Simple Server Daemon is a very simple server controller for the Il-2 Sturmovik 1946 dedicated server, born from the desire for an easily maintained tool with IL-2 DCG integration."
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [com.oracle/javafx-runtime "2.2.0"]
                 [com.airhacks/afterburner.fx "1.3"]
                 [commons-lang/commons-lang "2.3"]
                 [instaparse/instaparse "1.2.13"]
                 [com.brainbot/iniconfig "0.2.0"]
                 [junit/junit "3.8.1"]]
  :plugins [[lein-marginalia "0.7.1"]]
  :main uk.org.il2ssd.core
  :aot [uk.org.il2ssd.core]
  :source-paths ["src/clojure"]
  :java-source-paths ["src/java"]
  :test-paths ["test/clojure" "test/java"]
  :resource-paths ["resources" "src/java"]
  :global-vars {*warn-on-reflection* true}
  :profiles {:uberjar {:aot :all}})