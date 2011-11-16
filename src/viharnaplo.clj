(ns viharnaplo
  (:use [viharnaplo.config])
  (:require [viharnaplo.storm :as viharnaplo])
  (:require [accession.core :as redis]))

(defn- redis-flush []
  (redis/with-connection *redis* (redis/flushdb)))

(defn -main []
  (redis-flush)
  (viharnaplo/run 0))
