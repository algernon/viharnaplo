(ns redisdump
  "A simple script to dump the contents of a redis database, which is
being filled by the Viharnaplo Storm application."
  (:use [viharnaplo.config])
  (:require [accession.core :as redis])
  (:gen-class))

;; Get all keys matching the given <code>prefix</code> from redis,
;; iterate over them, get their value too, print both.
(defn- redis-dump
  [prefix]
  (let [dbkeys (redis/with-connection *redis* (redis/keys (str prefix ".*")))]
    (doseq [dbkey dbkeys]
      (println (str dbkey ":") (redis/with-connection *redis* (redis/get dbkey))))))

;; Main entry point, dump <code>program.*</code> and
;; <code>host.*</code> keys.
;;
;; Usage:
;; <pre><code>$ lein run -m redisdump</code></pre>
(defn -main []
  (redis-dump "program")
  (redis-dump "host"))
