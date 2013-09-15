(ns nasa-data-browser.handler
  (:use [compojure.core]
        [ring.middleware.resource]
        [ring.middleware.file-info])
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [compojure.response :as response]
            [clojure.java.io :as io]
            [nasa-data-browser.templates :as templates]
            [nasa-data-browser.parameters :as parameters]
            [nasa-data-browser.products :as products]
            [nasa-data-browser.variables :as variables]
            [nasa-data-browser.programs :as programs]
            [nasa-data-browser.datasets :as datasets]
            [nasa-data-browser.comparison :as comparison]
            [nasa-data-browser.info :as info]
            [nasa-data-browser.utils :as u]))
(def endpoint "http://localhost:8080/openrdf-sesame/repositories/nasa")
(def compiled "http://localhost:8080/openrdf-sesame/repositories/compile")
(comment (def endpoint "http://nasa-sesame.elasticbeanstalk.com/repositories/nasa"))
(comment (def compiled "http://nasa-sesame.elasticbeanstalk.com/repositories/compile"))

(defroutes app-routes
  (GET "/templates/:view" [view]       
       (-> (templates/get-data view) u/json-response))
  (GET "/parameters" []
       (-> (parameters/get-data compiled) u/json-response))
  (GET "/products" []
       (-> (products/get-data compiled) u/json-response))
  (GET "/variables" {params :params}
       (if (every? params [:parameter :keyword])
         (-> (variables/get-data (:parameter params) 
                                 (:keyword params)
                                 compiled)
             u/json-response)
         (if (contains? params :parameter)           
           (-> (variables/get-data (:parameter params) compiled)
               u/json-response))))
  (GET "/details" {params :params}
       (-> (variables/get-details (:variableName params) endpoint)
           u/json-response))
  (POST "/variables" {params :params}
        (if (every? params [:name :datasets :filterValues])
          (-> (variables/create (:name params)
                                (:datasets params)
                                (:filterValues params)
                                endpoint
                                compiled)
              u/json-response)))
  (GET "/programs" []
       (-> (programs/get-data endpoint) u/json-response))
  (GET "/datasets/:program" [program]
       (-> (datasets/get-data program endpoint) u/json-response))
  (GET "/comparison" {{vars :vars} :params}
       (-> (comparison/get-data vars endpoint) u/json-response))
  (GET "/info/:item" [item]
       (-> (info/get-data item endpoint) u/json-response))
  (route/resources "/")
  (route/not-found "Not Found"))
  
(def app
  (-> app-routes
      handler/site))