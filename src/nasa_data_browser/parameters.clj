(ns nasa-data-browser.parameters
  (:use [seabass core])
  (:require [nasa-data-browser.utils :as u]))
(def hierarchy-query
  (str u/prefix "
select distinct ?parameter ?filter ?filterValue
  ?parameterLabel ?filterLabel ?filterValueLabel ?relativeFilter { 
  ?parameterUri rdfs:subClassOf :ScienceParameter .
  ?parameterUri rdfs:label ?parameterLabel .
  ?filterUri :searchFilterFor ?parameterUri . 
  optional { ?filterUri rdfs:label ?filterLabel } .
  ?spec :parameter/rdfs:subClassOf ?parameterUri . 
  ?spec ?filterUri ?filterValueUri .
  ?filterValueUri rdfs:label ?filterValueLabel .
  bind (strafter(str(?parameterUri), '#') as ?parameter)
  bind (strafter(str(?filterUri), '#') as ?filter)
  bind (strafter(str(?filterValueUri), '#') as ?filterValue)
  bind (concat(?parameter, '#', ?filter) as ?relativeFilter)
} order by ?parameterLabel ?parameter
"))
(defn get-data [endpoint]
  (let [facts (:data (bounce hierarchy-query endpoint))
        parameters (into #{} (map :parameter facts))
        filters (u/build-relation :parameter :relativeFilter facts)
        filter-values (u/build-relation :relativeFilter :filterValue facts)
        name-sets (merge-with
                   u/set-union
                   (u/build-relation :parameter :parameterLabel facts)
                   (u/build-relation :relativeFilter :filterLabel facts)
                   (u/build-relation :filterValue :filterValueLabel facts))
        names (u/map-on-vals name-sets u/from-set)]
    (letfn [(get-filter [f]
              (let [name (get names f)]
                {"filter" f
                 "name" (if (nil? name) f name)
                 "values" (get filter-values f)}))
            (get-filters [p] (map get-filter (get filters p)))
            (get-parameter [p]
              (let [name (get names p)]
                {"parameter" (if (nil? name) p name)
                 "filters" (get-filters p)}))]
      {"parameters" (map get-parameter parameters)})))