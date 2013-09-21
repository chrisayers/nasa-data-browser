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

(def var-name-pull (str u/prefix "
construct { ?variableNameUri a :VariableName ;
                         :paramClass ?paramClass ;
                         :name ?variableName ;
                         :description ?label ;
                         :project ?project ;
                         :filters ?filterObjects ;
                         :variables ?variables .
} where {
 ?paramClass :properDirectSubclassOf :ScienceParameter .
 ?variableUri :parameter/rdfs:subClassOf* ?paramClass ;
              :project ?projectUri ;
              :variableName ?variableNameUri ;
              rdfs:label ?label .
 optional { ?variableNameUri rdfs:label ?variableLabel } .
 optional { ?projectUri rdfs:label ?projectName } .
 bind (strafter(str(?variableNameUri), '#') as ?variableNameTerm) .
 bind (coalesce(?variableLabel, ?variableNameTerm) as ?variableName) .
 bind (strafter(str(?projectUri), '#') as ?projectTerm) .
 bind (coalesce(?projectName, ?projectTerm) as ?project) .
 { select ?variableNameUri (group_concat(?varInfo; separator=',,,') as ?variables) {
   { select distinct ?variableNameUri ?varInfo {
     ?variableUri :variableName ?variableNameUri . 
     optional { ?variableUri :dataSet ?datasetUri .
                ?datasetUri rdfs:label ?datasetName .
                bind (strafter(str(?datasetUri), '#') as ?datasetTerm) } 
     bind (strafter(str(?variableUri), '#') as ?variableTerm) 
     bind (coalesce(?datasetName, ?datasetTerm, ?variableTerm) as ?dataset) 
     bind (concat(?variableTerm, '#', ?dataset) as ?varInfo) 
   } order by ?dataset}
 } group by ?variableNameUri }
 { select ?variableNameUri (group_concat(?filterObject; separator=',,,') as ?filterObjects) {
   { select distinct ?variableNameUri ?filterObject {
     ?variableUri ?filterUri ?objectUri . 
     ?variableUri :variableName ?variableNameUri .
     ?filterUri :searchFilterFor ?x .
     filter(?filterUri != :instrument) .
     bind (strafter(str(?filterUri), '#') as ?filter)
     bind (strafter(str(?objectUri), '#') as ?object)
     bind (concat(?filter, '#', ?object) as ?filterObject)
   } order by ?filterObject }
  } group by ?variableNameUri }
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

(defn materialize [endpoint filepath]
  (stash (build
          (pull var-pull endpoint)
          (pull var-name-pull endpoint)
          (pull param-pull endpoint)
          (pull prod-pull endpoint))
         filepath))

(comment
(def dev "http://localhost:8080/openrdf-sesame/repositories/nasa")
(def prod "http://nasa-sesame.elasticbeanstalk.com/repositories/nasa") 
(materialize dev "/Users/ryan/compiled.nt")
)