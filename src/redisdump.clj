(ns redisdump
  (:use [viharnaplo.config])
  (:require [accession.core :as redis])
  (:gen-class))

(defn- redis-dump [prefix]
  (let [dbkeys (redis/with-connection *redis* (redis/keys (str prefix ".*")))]
    (doseq [dbkey dbkeys]
      (println (str dbkey ":") (redis/with-connection *redis* (redis/get dbkey)))
      )))

(defn -main []
  (redis-dump "program")
  (redis-dump "host"))
