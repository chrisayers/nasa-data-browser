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

(defn get-variable-info [result]
  {"variable" (:variable result)
   "shortName" (:shortName result)
   "paramName" (:paramName result)})
(defn get-data [parameter endpoint]
  (let [facts (-> parameter query (bounce ,,, endpoint) :data)
        variables (map get-variable-info facts)
        filters (u/build-relation :filterObject :variable facts)]
    {"variables" variables
     "filterIndex" filters}))