(ns nasa-data-browser.utils
  (:require [clojure.set :as set]            
            [clojure.data.json :as json]))

(def prefix  "prefix : <http://www.flyingsandbox.com/2012/es#> 
              prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ")
(defn from-set [s]
  (if (set? s) (first s) s))
(defn map-on-vals [m f]
  (into {} (for [[k v] m] [k (f v)])))
(defn to-set [s]
  (if (set? s) s #{s}))
(defn set-union [s1 s2]
  (set/union (to-set s1) (to-set s2)))
(defn pull-relation [parent-key child-key m]
  {(get m parent-key) (-> (get m child-key) to-set)})
(defn build-relation [parent-key child-key result-set]
  (reduce #(merge-with set/union %1 %2)
          {}
          (map #(pull-relation parent-key child-key %1) result-set)))
(defn json-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body (json/write-str data)})