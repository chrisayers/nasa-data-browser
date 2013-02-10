(ns nasa-data-browser.variables
  (:use [seabass core])
  (:require [nasa-data-browser.utils :as u]))

(defn var-query [parameter]
  (str u/prefix "
select distinct ?variable ?variableName ?parameter ?paramName { 
 ?parameterUri rdfs:subClassOf :"parameter" .
 ?variableUri :parameter ?parameterUri .
 ?variableUri ?filterUri ?objectUri . 
 ?filterUri :searchFilterFor ?x .
 optional { ?variableUri rdfs:label ?variableName } .
 optional { ?parameterUri rdfs:label ?paramName } .
 bind (strafter(str(?variableUri), '#') as ?variable)
 bind (strafter(str(?parameterUri), '#') as ?parameter)
} order by ?variable
"))
(defn filt-query [parameter]
  (str u/prefix "
select distinct ?variable ?filterObject { 
 ?parameterUri rdfs:subClassOf :"parameter" .
 ?variableUri :parameter ?parameterUri .
 ?variableUri ?filterUri ?objectUri . 
 ?filterUri :searchFilterFor ?x .
 bind (strafter(str(?variableUri), '#') as ?variable)
 bind (strafter(str(?filterUri), '#') as ?filter)
 bind (strafter(str(?objectUri), '#') as ?object)
 bind (concat(?filter, '#', ?object) as ?filterObject)
} order by ?variable
"))

(defn get-data [parameter endpoint]
  (let [facts (-> parameter var-query (bounce endpoint) :data)
        vars (into #{} (map :variable facts))
        var-names (u/build-relation :variable :variableName facts)
        params (u/build-relation :variable :parameter facts)
        param-names (u/build-relation :parameter :paramName facts)
        filt-facts (-> parameter filt-query (bounce endpoint) :data)
        filts (u/build-relation :filterObject :variable filt-facts)]
    (letfn [(get-var-info [var]
              (let [param (-> (get params var) first)
                    param-name (-> (get param-names param) first)
                    var-name (-> (get var-names var) first)]
                {"uuid" var
                 "variable" var
                 "variableName" (if (nil? var-name) var var-name)
                 "parameter" param
                 "parameterName" (if (nil? param-name) param param-name)}))]
      {"filterIndex" filts
       "variables" (map get-var-info vars)})))