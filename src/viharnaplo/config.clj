(ns viharnaplo.config
  "Viharnaplo configuration module.

This doesn't do anything useful, but define a global redis
connection."
  (:require [accession.core :as redis])
  (:gen-class)
  )

;; This is the global redis connection.
;;
;; It would actually be cleaner if it was only a param hash, and the
;; various modules that need to connect to redis, would do so on their
;; own.
(def *redis* (redis/defconnection {}))
