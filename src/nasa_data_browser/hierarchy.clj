(ns nasa-data-browser.hierarchy
  (:use [seabass core])
  (:require [clojure.set :as set]))
(def prefix (str "prefix : <http://www.flyingsandbox.com/2012/es#> "
                 "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>"))
(def hierarchy-query
  (str prefix "
select distinct ?sciparam ?topic ?filter ?subFilter { 
  ?topic rdfs:subClassOf* :ScienceParameter . 
  ?filter :searchFilterFor ?topic . 
  ?variable ?filter ?subFilter . 
  ?subTopic rdfs:subClassOf* ?topic . 
  ?variable :parameter ?subTopic .
  bind (:ScienceParameter as ?sciparam)
}
"))     
(defn to-set [s]
  (if (set? s) s #{s}))
(defn set-union [s1 s2]
  (set/union (to-set s1) (to-set s2)))
(defn pull-hierarchy [parent-key child-key m]
  {(get m parent-key) (get m child-key)})
(defn build-hierarchy [parent-key child-key result-set]
  (reduce #(merge-with set-union %1 %2)
          {}
          (map #(pull-hierarchy parent-key child-key %1) result-set)))

(defn get-data [endpoint]
  (let [facts (:data (bounce hierarchy-query endpoint))]
    (merge-with set/union
     (build-hierarchy :sciparam :topic facts)
     (build-hierarchy :topic :filter facts)
     (build-hierarchy :filter :subFilter facts)
     )))   