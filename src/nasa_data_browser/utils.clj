(ns nasa-data-browser.utils
  (:require [clojure.set :as set]            
            [clojure.data.json :as json])
  (:use [clojure.string :only (split)]))

(def prefix  "prefix : <http://www.flyingsandbox.com/2013/nasa-es#> 
              prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ")
(defn rdf [x] (str "http://www.w3.org/1999/02/22-rdf-syntax-ns#" x))
(defn rdfs [x] (str "http://www.w3.org/2000/01/rdf-schema#" x))
(defn fly [x] (str "http://www.flyingsandbox.com/2013/nasa-es#" x))

(defn from-set [s]
  (if (set? s) (first s) s))
(defn map-on-vals [m f]
  (into {} (for [[k v] m] [k (f v)])))
(defn to-set [s]
  (if (set? s) s #{s}))
(defn set-union [s1 s2]
  (set/union (to-set s1) (to-set s2)))
(defn pull-relation 
  ([parent-key child-key m]
     {(if (vector? parent-key)
        (pmap #(get m %) parent-key)
        (get m parent-key))
      (-> (get m child-key) to-set)})
  ([parent-key parent-separator child-key m]
     (reduce #(merge-with set/union %1 %2)
             {}
             (pmap #(assoc {} %1 (-> (get m child-key) to-set)) 
                   (split (get m parent-key) parent-separator)))))
(defn build-relation 
  ([parent-key child-key result-set]
     (reduce #(merge-with set/union %1 %2)
             {}
             (pmap #(pull-relation parent-key child-key %1) result-set)))
  ([parent-key parent-separator child-key result-set]
     (let [sep (java.util.regex.Pattern/compile parent-separator)]
       (reduce #(merge-with set/union %1 %2)
               {}
               (pmap #(pull-relation parent-key sep child-key %1) result-set)))))
(defn json-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body (json/write-str data)})