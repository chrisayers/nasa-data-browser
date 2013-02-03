(ns nasa-data-browser.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [nasa-data-browser.parameters :as parameters]
            [nasa-data-browser.variables :as variables]
            [nasa-data-browser.info :as info]
            [nasa-data-browser.utils :as u]
            ))
(def endpoint "http://localhost:8080/openrdf-sesame/repositories/nasa")
(comment (def endpoint "http://sesame-sleepydog.elasticbeanstalk.com/repositories/nasa"))
(defroutes app-routes
  (route/files "/" {:root "public"})
  (GET "/parameters" []
       (-> (parameters/get-data endpoint)
           u/json-response))
  (GET "/variables" [parameter]
       (-> (variables/get-data parameter endpoint)
           u/json-response))
  (GET "/info" [variable]
       (-> (info/get-data variable endpoint)
           u/json-response))
  (route/not-found "Not Found"))
  
(def app
  (handler/site app-routes))
