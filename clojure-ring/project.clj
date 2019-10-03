(defproject clojure-sequential "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [ring/ring-core "1.7.1"]
                 [ring/ring-jetty-adapter "1.7.1"]
                 [compojure "1.6.1"]
                 [seancorfield/next.jdbc "1.0.8"]
                 [org.xerial/sqlite-jdbc "3.28.0"]]
  :main ^:skip-aot clojure-ring.core
  :target-path "target/%s"
  :global-vars {*warn-on-reflection* true}
  :profiles {:uberjar {:aot :all}})
