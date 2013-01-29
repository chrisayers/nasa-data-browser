(ns nasa-data-browser.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.data.json :as json]
            [nasa-data-browser.hierarchy :as hierarchy]
            [nasa-data-browser.variables :as variables]
            ))

(def endpoint "http://localhost:8080/openrdf-sesame/repositories/nasa")

(defn json-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body (json/write-str data)})

(defroutes app-routes
  (route/files "/" {:root "public"})
  (GET "/hierarchy" [] (-> endpoint hierarchy/get-data json-response))
  (GET "/variables" [] (-> endpoint variables/get-data json-response))
  (route/not-found "Not Found"))
  
(def app
  (handler/site app-routes))
