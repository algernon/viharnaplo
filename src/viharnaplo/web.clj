(ns viharnaplo.web
  "Viharnaplo web service.

The web service does two things: serve static files from
**resources/public/** (where the presentation is kept), and dump data
from redis in json format."
  (:use [viharnaplo.config]
        [ring.middleware.content-type])
  (:require [accession.core :as redis]
            [compojure.core :as compojure]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [clojure.data.json :as json])
  (:gen-class))

;; The core of the web service is a function that, given a prefix,
;; retrieves all keys with that prefix from redis, along with their
;; value, and returns a JSON representation of the result.
(defn- redis-to-json
  "Given a prefix, retrieve all keys from redis that start with this
  string, along with their values, and return the result in JSON
  format."
  [prefix]
  (let [dbkeys (redis/with-connection *redis*
                 (redis/keys (str prefix ".*")))
        dbvals (map #(redis/with-connection *redis* (redis/get %)) dbkeys)]
    (json/json-str (zipmap dbkeys dbvals))))

;; The next step is setting up the routes:
;;
;; * **/host.json** Shall return a map of _host.*_ keys, in JSON.
;; * **/program.json** Shall do the same, for _program.*_ keys.
;; * Everything else is looked up under _resources/public/_.
(compojure/defroutes main-routes
  (compojure/GET "/host.json" [] (redis-to-json "host"))
  (compojure/GET "/program.json" [] (redis-to-json "program"))
  (route/files "/" {:root "resources/public"}))

;; And in the end, we define the entry point, which simply asks
;; Compojure to serve our routes, carefully wrapped in a middleware
;; that sets the content-type based on the filename extension.
(def app
  (-> (wrap-content-type
      (handler/site main-routes))))
