(ns nasa-data-browser.variables
  (:use [seabass core])
  (:require [nasa-data-browser.utils :as u]))

(defn var-query 
  ([parameter]
     (str u/prefix "
select distinct ?variable ?variableName ?parameter ?paramName ?filterObjects { 
 ?parameterUri rdfs:subClassOf* :"parameter" .
 ?variableUri :parameter ?parameterUri .
 { select ?variableUri (group_concat(?filterObject; separator=',,,') as ?filterObjects) {
   { select distinct ?variableUri ?filterObject {
     ?variableUri ?filterUri ?objectUri . 
     ?filterUri :searchFilterFor ?x .
     bind (strafter(str(?filterUri), '#') as ?filter)
     bind (strafter(str(?objectUri), '#') as ?object)
     bind (concat(?filter, '#', ?object) as ?filterObject)
   }}
  } group by ?variableUri }
 optional { ?variableUri :variableName/rdfs:label ?alt1 } .
 optional { ?variableUri rdfs:label ?alt2 } .
 optional { ?parameterUri rdfs:label ?paramName } .
 bind (strafter(str(?variableUri), '#') as ?variable) .
 bind (coalesce(?alt1, ?alt2, ?variable, 'missing') as ?variableName) .
 bind (lcase(?variableName) as ?lcVarName) .
 bind (strafter(str(?parameterUri), '#') as ?parameter) .
} order by desc(?lcVarName)
"))
  ([parameter keyword]
     (str u/prefix "
select distinct ?variable ?variableName ?parameter ?paramName ?filterObjects { 
 ?parameterUri rdfs:subClassOf* :"parameter" .
 ?variableUri :parameter ?parameterUri .
 { select ?variableUri (group_concat(?filterObject; separator=',,,') as ?filterObjects) {
   { select distinct ?variableUri ?filterObject {
     ?variableUri ?filterUri ?objectUri . 
     ?filterUri :searchFilterFor ?x .
     bind (strafter(str(?filterUri), '#') as ?filter)
     bind (strafter(str(?objectUri), '#') as ?object)
     bind (concat(?filter, '#', ?object) as ?filterObject)
   }}
  } group by ?variableUri } 
 optional { ?variableUri :variableName/rdfs:label ?alt1 } .
 optional { ?variableUri rdfs:label ?alt2 } .
 optional { ?parameterUri rdfs:label ?paramName } .
 bind (strafter(str(?variableUri), '#') as ?variable) .
 bind (coalesce(?alt1, ?alt2, ?variable, 'missing') as ?variableName) .
 bind (lcase(?variableName) as ?lcVarName) .
 bind (strafter(str(?parameterUri), '#') as ?parameter) .
 filter(contains(lcase(?variableName), lcase('"keyword"'))) .
} order by desc(?lcVarName)
")))


(defn process-results [facts]
     (let [
           vars (distinct (into [] (map :variable facts)))
           var-names (u/build-relation :variable :variableName facts)
           alt-names (u/build-relation :variable :altVarName facts)
           params (u/build-relation :variable :parameter facts)
           param-names (u/build-relation :parameter :paramName facts)
           filts (u/build-relation :filterObjects ",,," :variable facts)]
     (letfn [(get-var-info [var]
               (let [param (-> (get params var) first)
                     param-name (-> (get param-names param) first)
                     var-name (-> (get var-names var) first)]
                 {"uuid" var
                  "variable" var
                  "variableName" var-name
                  "parameter" param
                  "parameterName" (if (nil? param-name) param param-name)}))]
       {"filterIndex" filts
        "variables" (map get-var-info vars)})))
(defn get-data
  ([parameter endpoint]
     (-> parameter var-query (bounce endpoint) :data process-results))
  ([parameter keyword endpoint]
     (-> (var-query parameter keyword) (bounce endpoint) :data process-results)))
         