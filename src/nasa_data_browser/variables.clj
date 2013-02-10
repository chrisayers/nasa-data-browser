(ns nasa-data-browser.variables
  (:use [seabass core])
  (:require [nasa-data-browser.utils :as u]))

(defn query [parameter]
  (str u/prefix "
select distinct ?variable ?variableName ?parameter ?paramName 
                ?filterObject ?product ?productName { 
 ?parameter rdfs:subClassOf :"parameter" .
 ?variableUri :parameter ?parameter .
 ?variableUri :product ?product  .
 optional { ?variableUri rdfs:label ?variableName } .
 optional { ?parameter rdfs:label ?paramName } .
 optional { ?product rdfs:label ?productName } .
 ?variableUri ?filterUri ?objectUri . 
 ?filterUri :searchFilterFor ?x .
 bind (strafter(str(?variableUri), '#') as ?variable)
 bind (strafter(str(?filterUri), '#') as ?filter)
 bind (strafter(str(?objectUri), '#') as ?object)
 bind (concat(?filter, '#', ?object) as ?filterObject)
} order by ?variable
"))

(defn get-data [parameter endpoint]
  (let [facts (-> parameter query (bounce ,,, endpoint) :data)
        vars (into #{} (map :variable facts))
        var-names (u/build-relation :variable :variableName facts)
        params (u/build-relation :variable :parameter facts)
        param-names (u/build-relation :variable :paramName facts)
        products (u/build-relation :variable :product facts)
        product-names (u/build-relation :variable :productName facts)
        filters (u/build-relation :filterObject :variable facts)]
    (letfn [(get-var-info [var]
              (let [var-name (-> (get var-names var) first)
                    param (-> (get params var) first)
                    param-name (-> (get param-names var) first)
                    product (-> (get products var) first)
                    product-name (-> (get product-names var) first)]
                {"uuid" var
                 "variable" (if (nil? var-name) var var-name)
                 "parameter" (if (nil? param-name) param param-name)
                 "products" (get products var)}))]
      {"variables" (map get-var-info vars)
       "filterIndex" filters})))