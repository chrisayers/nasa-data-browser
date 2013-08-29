(ns nasa-data-browser.programs
  (:use [seabass core])
  (:require [nasa-data-browser.utils :as u]))

(def programs-query 
  (str u/prefix "
select distinct ?program ?name {
?program a/rdfs:subClassOf* :Program .
?program rdfs:label ?name
}
"))

(defn get-data [endpoint]
  (let [facts (-> programs-query (bounce endpoint) :data)
        programs (into #{} (map :program facts))
        names (u/build-relation :program :name facts)]
    (letfn [(get-program [p]
              (let [name (get names p)]
                {"program" p
                 "name" name}))]
      {"programs" (map get-program programs)})))