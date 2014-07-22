(defproject trellolib "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [clj-oauth "1.5.1"]
                 [clj-http "0.9.2"]
                 [cheshire "5.3.1"]]
  :profiles {:dev {:dependencies [[midje "1.6.3"]]}})
