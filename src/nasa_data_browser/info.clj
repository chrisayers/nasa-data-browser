(ns nasa-data-browser.info
  (:use [seabass core])
  (:require [nasa-data-browser.utils :as u]))

(defn name-query [variable]
  (str u/prefix "
select distinct ?label { :"variable" rdfs:label ?label }
"))
(defn fact-query [variable]
  (str u/prefix "
select distinct ?predicate ?object ?lit { 
 :"variable" ?predicateUri ?objectUri . 
 ?predicateUri a :ESProperty . 
 optional { ?objectUri rdfs:label ?lit }
 bind (strafter(str(?predicateUri), '#') as ?predicate)
 bind ( if(contains(str(?objectUri), '#'), 
           strafter(str(?objectUri), '#'),
           ?objectUri)
        as ?object)
} order by ?predicate ?object
"))
(defn get-data [variable endpoint]
  (let [name (-> variable name-query (bounce ,,, endpoint) :data first :label)
        facts (-> variable fact-query (bounce ,,, endpoint) :data)]
    {"name" name
     "facts" facts}))