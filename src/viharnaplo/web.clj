(ns viharnaplo.web
  (:use [viharnaplo.config]
        [ring.middleware.content-type])
  (:require [accession.core :as redis]
            [compojure.core :as compojure]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [clojure.data.json :as json])
  (:gen-class))

(defn- redis-to-json []
  (let [dbkeys (redis/with-connection *redis*
                 (redis/keys "*"))
        dbvals (map #(redis/with-connection *redis* (redis/get %)) dbkeys)]
    (json/json-str (zipmap dbkeys dbvals))
    ))

(compojure/defroutes main-routes
  (compojure/GET "/data.json" [] (redis-to-json))
  (route/files "/" {:root "resources/public"}))

(def app
  (-> (wrap-content-type
      (handler/site main-routes))))
