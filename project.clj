(defproject trellolib "0.1.0-SNAPSHOT"
  :description "A clojure library for interacting with Trello"
  :url "http://https://github.com/jbatch/trellolib"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [clj-http "0.9.2"]
                 [cheshire "5.3.1"]]
  :profiles {:dev {:dependencies [[midje "1.6.3"]]}})
