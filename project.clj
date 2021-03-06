(defproject trellolib "0.2.1-SNAPSHOT"
  :description "A clojure library for interacting with Trello"
  :url "http://https://github.com/jbatch/trellolib"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [http-kit "2.1.16"]
                 [cheshire "5.3.1"]
                 [clj-oauth "1.5.1"]]
  :profiles {:dev {:dependencies [[midje "1.6.3"]
                                  [ring/ring-core "1.3.0"]]}})
