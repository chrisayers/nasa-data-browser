(ns nasa-data-browser.variables
  (:use [seabass core])
  (:require [nasa-data-browser.utils :as u]))

(defn query [parameter]
  (str u/prefix "
select distinct ?variable ?shortName ?paramName ?filterObject { 
 ?variableUri a/rdfs:subClassOf* :VariableSpecification . 
 ?variableUri :parameter ?parameter . 
 ?parameter rdfs:subClassOf* :" parameter " .
 optional { ?variableUri :variableName ?shortName } . 
 optional { ?parameter rdfs:label ?paramName } . 
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
        shorts (u/build-relation :variable :shortName facts)
        params (u/build-relation :variable :paramName facts)
        filters (u/build-relation :filterObject :variable facts)]
    (letfn [(get-var-info [var]
              (let [short (-> (get shorts var) first)
                    param (-> (get params var) first)]
                {"uuid" var
                 "shortName" (if (nil? short) var short)
                 "paramName" (if (nil? param) var param)}))]
      {"variables" (map get-var-info vars)
       "filterIndex" filters})))