(ns nasa-data-browser.variables-ingest
  (:use [seabass core])
  (:require [nasa-data-browser.utils :as u]))
(def programs-query 
  (str/u/prefix "
select distinct ?program {
?program a/rdfs:subClassOf* :Program }
")