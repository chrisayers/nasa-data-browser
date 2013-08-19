(ns nasa-data-browser.compile
  (:use [seabass core])
  (:require [nasa-data-browser.utils :as u]))

(def var-pull (str u/prefix "
construct { ?variableUri a :Variable .
            ?variableUri :paramClass ?paramClass .
            ?variableUri :ontName ?variable .
            ?variableUri :varName ?variableName .
            ?variableUri :param ?parameter .
            ?variableUri :paramName ?paramName .
            ?variableUri :filters ?filterObjects } where {
 ?paramClass :properDirectSubclassOf :ScienceParameter .
 ?parameterUri rdfs:subClassOf* ?paramClass .
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
 bind (strafter(str(?parameterUri), '#') as ?parameter) .
}
"))

(comment (stash (pull var-pull endpoint) "/home/foo/vars.nt"))