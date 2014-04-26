(defproject uk.org.il2ssd/il2ssd "0.1.2-SNAPSHOT"
  :description "Il-2 Simple Server Daemon is a very simple server controller for the Il-2 Sturmovik 1946 dedicated server. It aims to be an easy-to-use tool with IL-2 DCG integration."
  :url "https://github.com/DavidGregory084/il2ssd"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.async "0.1.278.0-76b25b-alpha"]
                 [com.airhacks/afterburner.fx "1.4.4"]
                 [commons-lang/commons-lang "2.6"]
                 [instaparse/instaparse "1.3.2"]
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
  :scm {:name "git"
        :developerConnection "scm:git:https://github.com/DavidGregory084/il2ssd.git"
        :url "https://github.com/DavidGregory084/il2ssd.git"}
  :pom-addition [:developers [:developer
                              [:name "David Gregory"]
                              [:email "davidgregory084@gmail.com"]
                              [:timezone "0"]]])
