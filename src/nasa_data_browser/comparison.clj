(ns nasa-data-browser.comparison
  (:use [seabass core])
  (:require [nasa-data-browser.utils :as u]
            [clojure.string :as str :only (join split)]))

(defn query [variables]
    (str u/prefix "
select ?variable ?variableName ?rel ?relName ?value ?valueName {
?variableUri ?relUri ?value .
filter(?variableUri in ("variables")) .
?relUri rdf:type :QuickFact .
filter(?relUri not in (:product, :variableName)) .
optional { ?variableUri rdfs:label ?variableName } .
optional { ?relUri rdfs:label ?relName } .
optional { ?value rdfs:label ?valueName } .
bind (strafter(str(?variableUri), '#') as ?variable)
bind (strafter(str(?relUri), '#') as ?rel)
} 
"))

(defn get-data [vars endpoint]
  (let [wrapped (map #(str ":"%"") vars)
        facts (-> (str/join ", " wrapped) query (bounce ,,, endpoint) :data)
        variables (into #{} (map :variable facts))
        relations (into #{} (map :rel facts))
        variable-names (u/build-relation :variable :variableName facts)
        rel-names (u/build-relation :rel :relName facts)
        values (u/build-relation :rel :value facts)
        value-names (u/build-relation :value :valueName facts)]
    (letfn [(get-rel-info [rel]
              (let [rel-name (-> (get rel-names rel) first)
                    value (-> (get values rel) first)
                    value-trunc (-> value (str/split #"#") last)
                    value-name (-> (get value-names value) first)]
                {"relation" (if (nil? rel-name) rel rel-name)
                 "value" (if (nil? value-name) value-trunc value-name)}))
            (get-var-info [variable]
              (let [name (-> (get variable-names variable) first)]
                    {"variable" variable
                     "name" (if (nil? name) variable name)
                     "quickFacts" (->> relations (map get-rel-info) merge)}))]
      {"relations" (map #(-> % rel-names first) relations)
       "variables" (map get-var-info variables)})))