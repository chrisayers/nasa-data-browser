(ns nasa-data-browser.parameters
  (:use [seabass core])
  (:require [nasa-data-browser.utils :as u]))
(def hierarchy-query
  (str u/prefix "
select ?parameter ?filter ?filterValue
  ?parameterLabel ?filterLabel ?filterValueLabel ?relativeFilter { 
  ?parameterUri rdfs:subClassOf :ScienceParameter .
  ?filterUri :searchFilterFor ?parameterUri . 
  ?spec :parameter/rdfs:subClassOf ?parameterUri . 
  ?spec ?filterUri ?filterValueUri .
  optional { ?parameterUri rdfs:label ?parameterLabel } .
  optional { ?filterUri rdfs:label ?filterLabel } .
  optional { ?filterValueUri rdfs:label ?filterValueLabel } .
  bind (strafter(str(?parameterUri), '#') as ?parameter)
  bind (strafter(str(?filterUri), '#') as ?filter)
  bind (strafter(str(?filterValueUri), '#') as ?filterValue)
  bind (concat(?parameter, '#', ?filter) as ?relativeFilter)
} order by ?parameterLabel ?parameter
"))
(def product-query
  (str u/prefix "
select ?variable ?product ?productName {
  ?variableUri :product ?productUri .
  optional { ?productUri rdfs:label ?productName } .
  bind (strafter(str(?variableUri), '#') as ?variable) .
  bind (strafter(str(?productUri), '#') as ?product) }
"))
(defn get-data [endpoint]
  (let [facts (-> hierarchy-query (bounce endpoint) :data)
        parameters (into #{} (map :parameter facts))
        filters (u/build-relation :parameter :relativeFilter facts)
        filter-values (u/build-relation :relativeFilter :filterValue facts)
        name-sets (merge-with
                   u/set-union
                   (u/build-relation :parameter :parameterLabel facts)
                   (u/build-relation :relativeFilter :filterLabel facts)
                   (u/build-relation :filterValue :filterValueLabel facts))
        names (u/map-on-vals name-sets u/from-set)
        product-facts (-> product-query (bounce endpoint) :data)
        products (u/build-relation :variable :product product-facts)
        product-names (u/build-relation :product :productName product-facts)]
    (letfn [(get-filter [f]
              (let [name (get names f)]
                {"filter" f
                 "name" (if (nil? name) f name)
                 "values" (get filter-values f)}))
            (get-filters [p] (map get-filter (get filters p)))
            (get-parameter [p]
              (let [name (get names p)]
                {"parameter" p
                 "name"  (if (nil? name) p name)
                 "filters" (get-filters p)}))]
      {"parameters" (map get-parameter parameters)
       "hasProduct" products
       "hasProductName" product-names})))