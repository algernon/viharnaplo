(ns viharnaplo.config
  (:require [somnium.congomongo :as mongo]
            [accession.core :as redis])
  (:gen-class))

(def *msgdb* (mongo/make-connection "syslog"
                                    :host "localhost"
                                    :port 27017))

(def *redis* (redis/defconnection {}))
