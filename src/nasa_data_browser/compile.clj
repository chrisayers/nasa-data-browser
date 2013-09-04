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
            ?variableUri :product ?product .
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
 optional { select ?variableUri ?product 
   { ?variableUri :dataSet ?product } limit 1
 } 
 bind (strafter(str(?variableUri), '#') as ?variable) .
 bind (coalesce(?alt1, ?alt2, ?variable, 'missing') as ?variableName) .
 bind (strafter(str(?parameterUri), '#') as ?parameter) .
}
"))

(def param-pull (str u/prefix "
construct { _:x a :Parameter ;
               :parameter ?rootParameter ;
               :paramLabel ?parameterLabel ;
               :relativeFilter ?relativeFilter ;
               :filterLabel ?filterLabel ;
               :filterValue ?filterValue ;
               :valueLabel ?filterValueLabel }
where {
{ select distinct ?rootParameter ?parameterLabel ?relativeFilter
                  ?filterLabel ?filterValue ?filterValueLabel {
  ?parameterClass :properDirectSubclassOf :ScienceParameter .
  ?parameterUri rdfs:subClassOf* ?parameterClass . 
  ?filterUri :searchFilterFor ?parameterClass . 
  ?var :parameter ?parameterUri . 
  ?var ?filterUri ?filterValueUri .
  optional { ?parameterClass rdfs:label ?parameterName } .
  optional { ?filterUri rdfs:label ?filterName } .
  optional { ?filterValueUri rdfs:label ?filterValueName } .
  bind (strafter(str(?parameterClass), '#') as ?rootParameter) .
  bind (coalesce(?parameterName, ?rootParameter) as ?parameterLabel) .
  bind (strafter(str(?filterUri), '#') as ?filter) .
  bind (concat(?rootParameter, '#', ?filter) as ?relativeFilter) .
  bind (coalesce(?filterName, ?relativeFilter) as ?filterLabel) .
  bind (strafter(str(?filterValueUri), '#') as ?filterValue) .
  bind (coalesce(?filterValueName, ?filterValue) as ?filterValueLabel)
}}}
"))

(def prod-pull (str u/prefix "
construct { _:x a :Product ;
                :variable ?variable ;
                :product ?product ;
                :label ?productLabel } 
where {
{ select distinct ?variable ?product ?productLabel {
  ?variableUri :dataSet ?productUri .
  optional { ?productUri rdfs:label ?productName } .
  bind (strafter(str(?variableUri), '#') as ?variable) . 
  bind (strafter(str(?productUri), '#') as ?product) .
  bind (coalesce(?productName, ?product) as ?productLabel)
}}}
"))

(comment 
(def endpoint "http://nasa-sesame.elasticbeanstalk.com/repositories/nasa")
(stash (build
        (pull var-pull endpoint)
        (pull param-pull endpoint)
        (pull prod-pull endpoint))
       "/Users/ryan/compiled.nt")
)