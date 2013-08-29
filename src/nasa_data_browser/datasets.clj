(ns nasa-data-browser.datasets
  (:use [seabass core])
  (:require [nasa-data-browser.utils :as u]))

(defn datasets-query [program]
  (str u/prefix "
select distinct ?dataset ?name ?level {
?dataset :productOf :"program" .
?dataset rdfs:label ?name .
?dataset :productLevel/rdfs:label ?level
}
"))

(defn get-data [program endpoint]
  (let [facts (-> program datasets-query (bounce endpoint) :data)
        datasets (into #{} (map :dataset facts))
        names (u/build-relation :dataset :name facts)
        levels (u/build-relation :dataset :level facts)]
    (letfn [(get-dataset [d]
              (let [name (get names d)
                    level (get levels d)]
                {"dataset" d
                 "name" name
                 "level" level}))]
      {"datasets" (map get-dataset datasets)})))