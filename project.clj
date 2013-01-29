(defproject nasa-data-browser "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [compojure "1.1.5"]
                 [org.clojure/data.json "0.2.1"]
                 [seabass "2.0"]
                 ]
  :plugins [[lein-ring "0.8.2"]
            [lein-swank "1.4.4"]
            ]
  :ring {:handler nasa-data-browser.handler/app}
  :profiles {:dev {:dependencies [[ring-mock "0.1.3"]]}}
  )
