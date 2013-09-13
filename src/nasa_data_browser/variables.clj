(ns nasa-data-browser.variables
  (:use [seabass core])
  (:require [nasa-data-browser.utils :as u]))

(defn var-query 
  ([parameter]
     (str u/prefix "
select distinct ?variable ?variableName ?description ?project ?filterObjects { 
  ?v a :Variable ;
     :paramClass :"parameter" ;
     :varName ?variableName ;
     :description ?description ;
     :project ?project ;
     :filters ?filterObjects .
  bind (replace(str(?variableName), ' ', '') as ?variable) .
  bind (lcase(?variableName) as ?lcVarName) .
} order by desc(?lcVarName)
"))
  ([parameter keyword]
     (str u/prefix "
select distinct ?variable ?variableName ?description ?project ?filterObjects { 
  ?v a :Variable ;
     :paramClass :"parameter" ;
     :varName ?variableName ;
     :description ?description ;
     :filters ?filterObjects .
  bind (replace(str(?variableName), ' ', '') as ?variable) .
  bind (lcase(?variableName) as ?lcVarName) .
  filter(contains(lcase(?variableName), lcase('"keyword"'))) .
} order by desc(?lcVarName)
")))


(defn process-results [facts]
     (let [
           vars (distinct (into [] (map :variable facts)))
           var-names (u/build-relation :variable :variableName facts)
           descrips (u/build-relation :variable :description facts)
           projects (u/build-relation :variable :project facts)
           filts (u/build-relation :filterObjects ",,," :variable facts)]
     (letfn [(get-var-info [var]
               (let [var-name (-> (get var-names var) first)
                     descrip (-> (get descrips var) first)
                     project (-> (get projects var) first)]
                 {"variable" var
                  "variableName" var-name
                  "description" descrip
                  "project" project}))]
       {"filterIndex" filts
        "variables" (map get-var-info vars)})))
(defn get-data
  ([parameter endpoint]
     (-> parameter var-query (bounce endpoint) :data process-results))
  ([parameter keyword endpoint]
     (-> (var-query parameter keyword) (bounce endpoint) :data process-results)))

(defn hashcode [x] (hash (str x (java.util.Date.))))
         
(defn name-triples [name var-name-uuid]
  (let [t1 (resource-fact (u/fly var-name-uuid) (u/rdf "type") (u/fly "Name"))
        t2 (literal-fact (u/fly var-name-uuid) (u/rdfs "label") name)]
    (list t1 t2)))

(defn dataset-triples [dataset var-name-uuid var-dataset-uuid]
  (let [t1 (resource-fact (u/fly var-dataset-uuid) (u/rdf "type") (u/fly "VariableSpecification"))
        t2 (resource-fact (u/fly var-dataset-uuid) (u/fly "dataset") (u/fly dataset))
        t3 (resource-fact (u/fly var-dataset-uuid) (u/fly "variableName") (u/fly var-name-uuid))]
    (list t1 t2 t3)))

(defn filter-triples [var-dataset-uuid filter-value]
  (let [the-filter-value (.split filter-value "#")
        the-filter (first the-filter-value)
        the-value (last the-filter-value)
        t1 (resource-fact (u/fly var-dataset-uuid) (u/fly the-filter) (u/fly the-value))]
    (list t1)))

(defn all-filter-triples [var-dataset-uuid filter-values]
  (map #(filter-triples var-dataset-uuid %) filter-values))

(defn get-facts [variable-name datasets filter-values]
  (let [var-name-uuid (hashcode variable-name)
        dataset-map (zipmap datasets (map #(hashcode (str var-name-uuid %)) datasets))
        name-facts (name-triples variable-name var-name-uuid)
        dataset-facts (map #(dataset-triples % var-name-uuid (get dataset-map %)) datasets)
        filter-facts (map #(all-filter-triples (get dataset-map %) filter-values) datasets)]
    (flatten (list name-facts dataset-facts filter-facts))))

(defn create [variable-name datasets filter-values endpoint compiled]
  (let [facts (get-facts variable-name datasets filter-values)]
    (push endpoint facts)))
        