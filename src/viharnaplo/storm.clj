(ns viharnaplo.storm
  (:import [backtype.storm StormSubmitter LocalCluster])
  (:use [backtype.storm clojure config]
        [viharnaplo.config])
  (:require [accession.core :as redis])
  (:gen-class))

(defspout log-spout ["host", "program"] {:params [hosts, programs] :prepare false}
  [collector]
  (Thread/sleep (rand-int 10))
  (emit-spout! collector [(rand-nth hosts), (rand-nth programs)]))

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
   {1 (spout-spec
       (log-spout ["beren", "luthien", "treebeard", "galadriel", "bearg", "eowyn", "eresse", "hadhodrond", "durin"]
                  ["sshd", "ovpn-tun0", "CRON", "/USR/BIN/CRON" "postfix/master", "kernel"])
       )}
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
    (if (> length 0)
      (do
        (Thread/sleep length)
        (.shutdown cluster)))))
