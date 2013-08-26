(ns nasa-data-browser.parameters
  (:use [seabass core])
  (:require [nasa-data-browser.utils :as u]))
(def hierarchy-query
  (str u/prefix "
select distinct ?rootParameter ?relativeFilter ?filterValue 
                ?parameterLabel ?filterLabel ?filterValueLabel {
  ?x a :Parameter ; 
     :parameter ?rootParameter ;
     :paramLabel ?parameterLabel ;
     :relativeFilter ?relativeFilter ;
     :filterLabel ?filterLabel ;
     :filterValue ?filterValue ;
     :valueLabel ?filterValueLabel } 
"))
(def product-query
  (str u/prefix "
select ?variable ?product ?productName {
  ?x a :Product ;
     :variable ?variable ;
     :product ?product ;
     :label ?productName }
"))

(defn get-data [endpoint]
  (let [facts (-> hierarchy-query (bounce endpoint) :data)
        parameters (into #{} (map :rootParameter facts))
        filters (u/build-relation :rootParameter :relativeFilter facts)
        filter-values (u/build-relation :relativeFilter :filterValue facts)
        name-sets (merge-with
                   u/set-union
                   (u/build-relation :rootParameter :parameterLabel facts)
                   (u/build-relation :relativeFilter :filterLabel facts)
                   (u/build-relation :filterValue :filterValueLabel facts))
        names (u/map-on-vals name-sets u/from-set)
        product-facts (-> product-query (bounce endpoint) :data)
        products (u/build-relation :variable :product product-facts)
        product-names (u/build-relation :product :productName product-facts)]
    (letfn [(get-value [v]
              (let [name (get names v)]
                {"value" v
                 "valueName" name}))
            (get-filter [f]
              (let [name (get names f)]
                {"filter" f
                 "name" name
                 "values" (->> f (get filter-values) (map get-value))}))
            (get-filters [p] (map get-filter (get filters p)))
            (get-parameter [p]
              (let [name (get names p)]
                {"parameter" p
                 "name" name
                 "filters" (get-filters p)}))]
      {"parameters" (map get-parameter parameters)
       "hasProduct" products
       "hasProductName" product-names})))