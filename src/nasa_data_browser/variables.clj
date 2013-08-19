(ns nasa-data-browser.variables
  (:use [seabass core])
  (:require [nasa-data-browser.utils :as u]))

(defn var-query 
  ([parameter]
     (str u/prefix "
select distinct ?variable ?variableName ?parameter ?paramName ?filterObjects { 
  ?v a :Variable ;
     :paramClass :"parameter" ;
     :ontName ?variable ;
     :varName ?variableName ;
     :param ?parameter ;
     :paramName ?paramName ;
     :filters ?filterObjects .
  bind (lcase(?variableName) as ?lcVarName) .
} order by desc(?lcVarName)
"))
  ([parameter keyword]
     (str u/prefix "
select distinct ?variable ?variableName ?parameter ?paramName ?filterObjects { 
  ?v a :Variable ;
     :paramClass :"parameter" ;
     :ontName ?variable ;
     :varName ?variableName ;
     :param ?parameter ;
     :paramName ?paramName ;
     :filters ?filterObjects .
  bind (lcase(?variableName) as ?lcVarName) .
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
         