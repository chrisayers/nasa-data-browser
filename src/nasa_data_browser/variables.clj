(ns nasa-data-browser.variables
  (:use [seabass core])
  (:require [nasa-data-browser.utils :as u]))

(defn query [parameter]
  (str u/prefix "
select distinct ?variable ?variableName ?paramName ?filterObject ?product { 
 ?variableUri :parameter ?parameter .
 ?variableUri rdfs:label ?variableName .
 ?parameter rdfs:subClassOf :" parameter " .
 ?parameter rdfs:label ?paramName .
 ?variableUri ?filterUri ?objectUri . 
 ?filterUri :searchFilterFor ?x .
 optional { ?variableUri :product ?product } .
 bind (strafter(str(?variableUri), '#') as ?variable)
 bind (strafter(str(?filterUri), '#') as ?filter)
 bind (strafter(str(?objectUri), '#') as ?object)
 bind (concat(?filter, '#', ?object) as ?filterObject)
} order by ?variable
"))

(defn get-data [parameter endpoint]
  (let [facts (-> parameter query (bounce ,,, endpoint) :data)
        vars (into #{} (map :variable facts))
        names (u/build-relation :variable :variableName facts)
        params (u/build-relation :variable :paramName facts)
        products (u/build-relation :variable :product facts)
        filters (u/build-relation :filterObject :variable facts)]
    (letfn [(get-var-info [var]
              (let [name (-> (get names var) first)
                    param (-> (get params var) first)]
                {"uuid" var
                 "variableName" (if (nil? name) var name)
                 "paramName" (if (nil? param) var param)}))]
      {"variables" (map get-var-info vars)
       "filterIndex" filters})))