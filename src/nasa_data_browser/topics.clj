(ns nasa-data-browser.topics
  (:use [seabass core])
  (:require [nasa-data-browser.utils :as u]))
(def hierarchy-query
  (str u/prefix "
select distinct ?topic ?filter ?filterValue
  ?topicLabel ?filterLabel ?filterValueLabel ?relativeFilter { 
  ?topicUri rdfs:subClassOf :ScienceParameter .
  ?filterUri :searchFilterFor ?topicUri . 
  ?parameter :parameter/rdfs:subClassOf ?topicUri . 
  ?parameter ?filterUri ?filterValueUri .
  optional { ?topicUri rdfs:label ?topicLabel } 
  optional { ?filterUri rdfs:label ?filterLabel } 
  optional { ?filterValueUri rdfs:label ?filterValueLabel }
  bind (strafter(str(?topicUri), '#') as ?topic)
  bind (strafter(str(?filterUri), '#') as ?filter)
  bind (strafter(str(?filterValueUri), '#') as ?filterValue)
  bind (concat(?topic, '#', ?filter) as ?relativeFilter)
} order by ?topicLabel ?topic
"))     
(defn get-data [endpoint]
  (let [facts (:data (bounce hierarchy-query endpoint))
        topics (into #{} (map :topic facts))
        filters (u/build-relation :topic :relativeFilter facts)
        filter-values (u/build-relation :relativeFilter :filterValue facts)
        name-sets (merge-with
                   u/set-union
                   (u/build-relation :topic :topicLabel facts)
                   (u/build-relation :filter :filterLabel facts)
                   (u/build-relation :filterValue :filterValueLabel facts))
        names (u/map-on-vals name-sets u/from-set)]
    {"topics" topics
     "filters" filters
     "filterValues" filter-values
     "names" names}))