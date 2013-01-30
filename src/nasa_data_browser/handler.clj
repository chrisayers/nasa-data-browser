(ns nasa-data-browser.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [nasa-data-browser.topics :as topics]
            [nasa-data-browser.variables :as variables]
            [nasa-data-browser.info :as info]
            [nasa-data-browser.utils :as u]
            ))
(comment
  (def endpoint "http://localhost:8080/openrdf-sesame/repositories/nasa"))
(def endpoint "http://sesame-sleepydog.elasticbeanstalk.com/repositories/nasa")
(defroutes app-routes
  (route/files "/" {:root "public"})
  (GET "/topics" []
       (-> endpoint
           topics/get-data
           u/json-response))
  (GET "/variables:parameter" [parameter]
       (-> endpoint
           (variables/get-data parameter ,,,)
           u/json-response))
  (GET "/info:variable" [variable]
       (-> endpoint
           (variables/get-data variable ,,,)
           u/json-response))
  (route/not-found "Not Found"))
  
(def app
  (handler/site app-routes))
