(defproject nasa-data-browser "0.1"
  :description "Web app for browsing nasa data sources"
  :url "http://github.com/ryankohl/nasa-data-browser"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [compojure "1.1.5"]
                 [org.clojure/data.json "0.2.1"]
                 [seabass "2.0"]
                 ]
  :plugins [[lein-ring "0.8.2"]]
  :ring {:handler nasa-data-browser.handler/app
         :url-pattern "/data/*"}
  :profiles {:dev {:dependencies [[ring-mock "0.1.3"]]}}
  )
