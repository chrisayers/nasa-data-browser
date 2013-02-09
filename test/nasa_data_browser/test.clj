(ns nasa-data-browser.test
  (:use [seabass.core]
        [clojure.test])
  (:require [nasa-data-browser.parameters :as parameters]
            [nasa-data-browser.variables :as variables]))

(def endpoint (build "test/test.nt"))

(deftest parameters-test
  (let [data (parameters/get-data endpoint)
        params (get data "parameters")
        param (first params)
        filters (get param "filters")
        filt (first filters)]
  (-> param (get "parameter") (= "Radiation") is)
  (-> filters count (= 7) is)
  (-> filt (get "filter") (= "Radiation#targetSpectralRange") is)
  (-> filters first (get "name") (= "spectral range") is)
  (-> filters first (get "values") count (= 2) is)))

(deftest variables-test
  (let [data (variables/get-data "Radiation" endpoint)
        vars (get data "variables")
        var (first vars)
        filt-index (get data "filterIndex")]
    (-> vars count (= 138) is)
    (-> var (get "uuid") (= "MERRA-LWGEM") is)
    (-> var (get "variableName") (= "MERRA LWGEM") is)
    (-> var (get "paramName") (= "Flux") is)
    (-> filt-index (get "targetLocation#EarthSurface") count (= 58) is)))