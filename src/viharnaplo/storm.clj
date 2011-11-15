(ns viharnaplo.storm
  (:import [backtype.storm StormSubmitter LocalCluster])
  (:use [backtype.storm clojure config]
        [viharnaplo.config])
  (:require [somnium.congomongo :as mongo]
            [accession.core :as redis])
  (:gen-class))

(defn get-logs []
  (mongo/with-mongo *msgdb*
    (mongo/fetch :messages)))

(def log-iter (atom (get-logs)))

(defn get-log! []
  (let [old-item (first @log-iter)]
    (swap! log-iter rest)
    old-item))

(defspout mongolog-spout ["host", "program"]
  [conf context collector]
  (let [logs (get-logs)]
    (spout
     (nextTuple []
                (let [log (get-log!)]
                  (emit-spout! collector [(log :HOST), (log :PROGRAM)])))
     (ack [id]))))

(defbolt filter-interesting ["program"]
  [tuple collector]
  (let [program (.getString tuple 1)]
    (cond
     (= program "sshd") (emit-bolt! collector [program])
     (= program "ovpn-tun0") (emit-bolt! collector [program])
     ))
  (ack! collector tuple))

(defbolt incr-in-redis ["what"] {:params [prefix]}
  [tuple collector]
  (let [what (str prefix (.getString tuple 0))]
    (redis/with-connection *redis* (redis/incr what))
    (emit-bolt! collector [what] :anchor tuple)))

(defn mk-topology []
  (topology
   {1 (spout-spec mongolog-spout)}
   {2 (bolt-spec {1 :shuffle}
                 filter-interesting
                 :p 5)
    3 (bolt-spec {2 ["program"]}
                 (incr-in-redis "program.")
                 :p 2)
    4 (bolt-spec {1 ["host"]}
                 (incr-in-redis "host."))
    }
    ))

(defn run [length]
  (let [cluster (LocalCluster.)]
    (.submitTopology cluster "viharnaplo"
                     {
                      TOPOLOGY-DEBUG false
                      TOPOLOGY-WORKERS 10
                      }
                     (mk-topology))
    (Thread/sleep length)
    (.shutdown cluster)))
