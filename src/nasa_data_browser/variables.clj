(ns nasa-data-browser.variables
  (:use [seabass core]))
(def prefix (str "prefix : <http://www.flyingsandbox.com/2012/es#> "
                 "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>"))
(def flying "http://www.flyingsandbox.com/2012/es#")
(defn smallQuery [topic]
  (str prefix "
select ?parameter ?variable { 
  ?variable :parameter ?parameter .
  ?parameter rdfs:subClassOf* :" topic "
} order by ?parameter ?variable
"))
(defn get-data [topic endpoint]
  (-> (smallQuery topic) (bounce ,,, endpoint)))