(ns nasa-data-browser.test
  (:use [seabass.core]
        [clojure.test])
  (:require [nasa-data-browser.parameters :as parameters]
            [nasa-data-browser.variables :as variables]
            [nasa-data-browser.comparison :as comparison]))

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
    (-> vars count (= 120) is)
    (-> var (get "uuid") (= "SYNTunedTotalSky-NoAerosolLWTOAUp4") is)
    (-> var (get "variable") (= "Tuned Total-Sky-NoAerosol LW TOA Up") is)
    (-> var (get "parameter") (= "Flux") is)
    (-> var (get "products") (= #{"http://www.flyingsandbox.com/2012/es#CER_SYN1deg-3Hour_Terra-MODIS_Edition3A"}) is)
    (-> filt-index (get "targetLocation#EarthSurface") count (= 48) is)))

(deftest comparison-test
  (let [inputs ["MERRA-LWGEM", "SYNTunedClearSkyWNDown1", "MERRA-LWTUP"]
        data (comparison/get-data inputs endpoint)
        vars (get data "variables")
        rels (get data "relations")]
        (-> vars count (= 3) is)
        (-> vars first keys count (= 3) is)
        (-> vars first (get "quickFacts") count (= 9) is)
        (-> rels count (= 9) is)))
        
        