(ns nasa-data-browser.variables
  (:use [seabass core]
        [clojure.string :only (split)])
  (:require [nasa-data-browser.utils :as u]))

(defn var-query 
  ([parameter]
     (str u/prefix "
select distinct ?vn ?variableName ?description ?project ?filterObjects 
                ?variables ?variableTerm { 
  ?vn a :VariableName ;
     :paramClass :"parameter" ;
     :name ?variableName ;
     :description ?description ;
     :project ?project ;
     :filters ?filterObjects ;
     :variables ?variables .
  bind (strafter(str(?vn), '#') as ?variableTerm)
  bind (lcase(?variableName) as ?lcVarName) .
} order by desc(?lcVarName)
"))
  ([parameter keyword]
     (str u/prefix "
select distinct ?vn ?variableName ?description ?project ?filterObjects 
                ?variables  ?variableTerm { 
  ?vn a :VariableName ;
     :paramClass :"parameter" ;
     :name ?variableName ;
     :description ?description ;
     :project ?project ;
     :filters ?filterObjects ;
     :variables ?variables .
  bind (strafter(str(?vn), '#') as ?variableTerm)
  bind (lcase(?variableName) as ?lcVarName) .
  filter(contains(lcase(?variableName), lcase('"keyword"'))) .
} order by desc(?lcVarName)
")))

(def sep-term (java.util.regex.Pattern/compile "#"))
(def sep-terms (java.util.regex.Pattern/compile ",,,"))

(defn transform-var-info [var-info]
  (let [info (split var-info sep-term)]
    {"variable" (get info 0)
     "dataset" (get info 1)}))
        
(defn process-results [facts]
  (let [var-names (distinct (into [] (map :variableName facts)))
        var-terms (u/build-relation :variableName :variableTerm facts)
        descrips (u/build-relation :variableName :description facts)
        projects (u/build-relation :variableName :project facts)
        filts (u/build-relation :filterObjects ",,," :variableTerm facts)
        var-info (u/build-relation :variableName :variables facts)]
    (letfn [(get-var-info [vn]
              (let [var-term (-> (get var-terms vn) first)
                    descrip (-> (get descrips vn) first)
                    project (-> (get projects vn) first)
                    vars (-> (get var-info vn) first (split sep-terms))
                    variables (map transform-var-info vars)]
                {"variableName" var-term
                 "name" vn
                 "description" descrip
                 "project" project
                 "variables" variables }))]
      {"filterIndex" filts
       "variableNames" (map get-var-info var-names)})))
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
