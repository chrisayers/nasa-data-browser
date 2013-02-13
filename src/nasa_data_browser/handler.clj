(ns nasa-data-browser.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [compojure.response :as response]
            [clojure.java.io :as io]
            [nasa-data-browser.templates :as templates]
            [nasa-data-browser.parameters :as parameters]
            [nasa-data-browser.variables :as variables]
            [nasa-data-browser.comparison :as comparison]
            [nasa-data-browser.info :as info]
            [nasa-data-browser.utils :as u]
            ))
(def endpoint "http://localhost:8080/openrdf-sesame/repositories/nasa")
(comment (def endpoint "http://sesame-sleepydog.elasticbeanstalk.com/repositories/nasa"))

(defn wrap-content-type [handler content-type]
  (fn [request]
    (let [response (handler request)]
      (assoc-in response [:headers "Content-Type"] content-type))))

(defroutes app-routes
  (GET "/templates/:view" [view]       
       (-> (templates/get-data view)
           u/json-response))
  (GET "/parameters" []
       (-> (parameters/get-data endpoint)
           u/json-response))
  (GET "/variables/:parameter" [parameter]
       (-> (variables/get-data parameter endpoint)
           u/json-response))
  (GET "/comparison" {{vars :vars} :params}
       (-> (comparison/get-data vars endpoint)
           u/json-response))
  (GET "/info/:item" [item]
       (-> (info/get-data item endpoint)
           u/json-response))
  (GET "/" [] 
      (-> (io/resource "public/index.html") 
          (response/render {"Content-Type" "text/html"})))
  (route/resources "/")
  (route/not-found "Not Found"))
  
(def app
  (handler/site app-routes))
