(ns viharnaplo.config
  (:require [accession.core :as redis])
  (:gen-class)
  )

(def *redis* (redis/defconnection {}))
