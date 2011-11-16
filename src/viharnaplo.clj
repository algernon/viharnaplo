(ns viharnaplo
  (:use [viharnaplo.config])
  (:require [viharnaplo.storm :as viharnaplo]))

(defn -main []
  (viharnaplo/run 0))
