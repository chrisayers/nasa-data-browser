(ns nasa-data-browser.products
  (:use [seabass core])
  (:require [nasa-data-browser.utils :as u]))

(def product-query
  (str u/prefix "
select ?variable ?product ?productName {
  ?x a :Product ;
     :variable ?variable ;
     :product ?product ;
     :label ?productName }
"))

(defn get-data [endpoint]
  (let [product-facts (-> product-query (bounce endpoint) :data)
        products (u/build-relation :variable :product product-facts)
        product-names (u/build-relation :product :productName product-facts)]
    {"hasProduct" products
     "hasProductName" product-names}))