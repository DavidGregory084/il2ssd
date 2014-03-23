(defproject uk.org.il2ssd/il2ssd "0.1.0-SNAPSHOT"
  :description "Il-2 Simple Server Daemon is a very simple server controller for the Il-2 Sturmovik 1946 dedicated server. It aims to be an easy-to-use tool with IL-2 DCG integration."
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [com.airhacks/afterburner.fx "1.4.4"]
                 [commons-lang/commons-lang "2.6"]
                 [instaparse/instaparse "1.3.0"]
                 [com.brainbot/iniconfig "0.2.0"]
                 [overtone/at-at "1.2.0"]]
  :profiles {:dev {:dependencies [[org.apache.maven/maven-ant-tasks "2.1.3"]
                                  [junit/junit "4.11"]
                                  [org.loadui/testFx "3.1.2"]]}
             :junit {:java-source-paths ["src/java" "test/java"]}}
  :main uk.org.il2ssd.core
  :aot [uk.org.il2ssd.core]
  :source-paths ["src/clojure"]
  :java-source-paths ["src/java"]
  :test-paths ["test/clojure" "test/java"]
  :resource-paths ["resources" "src/java"]
  :global-vars {*warn-on-reflection* true}
  :scm {:name "hg"
        :developerConnection "scm:hg:ssh://hg@bitbucket.org/dgregory084/il-2-simple-server-daemon"
        :url "https://bitbucket.org/dgregory084/il-2-simple-server-daemon"})
