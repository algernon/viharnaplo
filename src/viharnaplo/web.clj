(ns viharnaplo.web
  (:use [viharnaplo.config]
        [ring.middleware.content-type])
  (:require [accession.core :as redis]
            [compojure.core :as compojure]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [clojure.data.json :as json])
  (:gen-class))

(defn- redis-to-json [prefix]
  (let [dbkeys (redis/with-connection *redis*
                 (redis/keys (str prefix ".*")))
        dbvals (map #(redis/with-connection *redis* (redis/get %)) dbkeys)]
    (json/json-str (zipmap dbkeys dbvals))))

(compojure/defroutes main-routes
  (compojure/GET "/host.json" [] (redis-to-json "host"))
  (compojure/GET "/program.json" [] (redis-to-json "program"))
  (route/files "/" {:root "resources/public"}))

(def app
  (-> (wrap-content-type
      (handler/site main-routes))))
