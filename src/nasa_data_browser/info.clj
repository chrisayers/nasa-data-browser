(ns nasa-data-browser.info
  (:use [seabass core])
  (:require [nasa-data-browser.utils :as u]
            [clojure.string :as str :only (join split)]))

(defn query [item]
  (str u/prefix "
select ?item ?rel ?value ?itemName ?relName ?valueName {
bind (:"item" as ?itemUri) .
?itemUri ?relUri ?value .
?relUri rdf:type :QuickFact .
OPTIONAL { ?itemUri rdfs:label ?itemName } .
OPTIONAL { ?relUri rdfs:label ?relName } .
OPTIONAL { ?value rdfs:label ?valueName }
bind (strafter(str(?itemUri), '#') as ?item)
bind (strafter(str(?relUri), '#') as ?rel)
}
"))
(defn get-data [item endpoint]
  (let [facts (-> item query (bounce ,,, endpoint) :data)
        item (-> facts first :item)
        item-name (-> facts first :itemName)
        relations (u/build-relation :item :rel facts)
        relation-names (u/build-relation :rel :relName facts)
        values (u/build-relation :rel :value facts)
        value-names (u/build-relation :value :valueName facts)]
    (letfn [(get-value-name [value]
              (let [value-trunc (-> value (str/split #"#") last)
                    value-name (-> (get value-names value) first)]
                (if (nil? value-name) value-trunc value-name)))
            (get-rel-info [rel]
              (let [rel-name (-> (get relation-names rel) first)
                    value (-> (map get-value-name (get values rel)))]
                {"relation" (if (nil? rel-name) rel rel-name)
                 "value" (if (= (count value) 1) (first value) value)}))]
      {"item" item
       "name" (if (nil? item-name) item item-name)
       "facts" (map get-rel-info (get relations item))})))