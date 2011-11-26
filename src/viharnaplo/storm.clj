(ns viharnaplo.storm
  "The Storm part of Viharnaplo.

This module is responsible for setting up the topology, with spouts
and bolts and everything else."
  (:import [backtype.storm StormSubmitter LocalCluster])
  (:use [backtype.storm clojure config]
        [viharnaplo.config])
  (:require [accession.core :as redis])
  (:gen-class))

;; Before everything else, we need a Spout. A spout is pretty much the
;; source of our data.
;;
;; In our case, we use a spout that takes two vectors,
;; <code>hosts</code> and <code>programs</code>, chooses one randomly
;; from each, and emits a tuple containing a host and a program.
;;
;; It also sleeps a little between emits, to make the graph a little
;; bit more interesting and perhaps more realistic.
(defspout log-spout ["host", "program"] {:params [hosts, programs] :prepare false}
  [collector]
  (Thread/sleep (rand-int 10))
  (emit-spout! collector [(rand-nth hosts), (rand-nth programs)]))

;; Next up, we want to filter programs, and only keep a few of
;; them. So we create a bolt, that takes a vector of
;; <code>interesting</code> program names, and whenever it receives
;; input, it checks whether the received tuple's program part is
;; interesting or not, and if it is, it emits the program name only.
;;
;; If it is not, it acknowledges the tuple, and ignores it.
(defbolt filter-interesting ["program"] {:params [interesting]}
  [tuple collector]
  (let [program (.getString tuple 1)]
    (if (some #(= program %) interesting) (emit-bolt! collector [program])))
  (ack! collector tuple))

;; Once we're done with pre-processing the data, and we're ready to
;; increase the appropriate counter, we want to do that. For the sake
;; of brevity, we use redis for this purpose: the key this bolt
;; receives gets prefixed with a string given at topology build time,
;; and that's used as the key whose value we increment in redis.
;;
;; We then emit the tuple further, in case there's something else
;; behind us.
(defbolt incr-in-redis ["what"] {:params [prefix]}
  [tuple collector]
  (let [what (str prefix (.getString tuple 0))]
    (redis/with-connection *redis* (redis/incr what))
    (emit-bolt! collector [what] :anchor tuple)))

;; Before building the topology, we want a small helper function...
;;
;; If we want to increase the frequency of an item in our generated
;; stream, the easiest way is to place it into the log-sput's vectors
;; multiple times. This small function helps accomplish that.
(defn with-prio
  "Take a value, repeat it n times, return the list."
  [v, n]
  (take n (repeat v)))


;; The core of the Storm application is the topology. In our simple
;; case, we have a single spout and only a handful of bolts.
;;
;; We want to count the number of messages per-host, and the number of
;; messages sent per program (but only a few selected programs).
;;
;; To accomplish this, we'll use a single spout, that is directly
;; connected to a counter, on the <code>host</code> field, so that
;; messages from the same host, will end up at the same counter. Since
;; the counter expects the key to use in its first argument, and the
;; log-spout's output has the host first, we're done, and this part
;; works.
;;
;; Next up, we want to filter programs, so we hook up the
;; filter-interesting bolt to the spout, and give it a short list of
;; programs we're interested in.
;;
;; Then we connect the same counter, on the <code>program</code> field
;; to the filter - which happens to emit only one value, the program
;; name. So conviniently, we can use the same counter function,
;; without much further thought.
;;
;; And that is our topology.
(defn mk-topology
  "Builds the simple Viharnaplo topology."
  []
  (topology
   {1 (spout-spec
       (log-spout (flatten [(with-prio "beren" 5), (with-prio "luthien" 2),
                            "treebeard", "galadriel", "bearg", "eowyn",
                            "eresse", "hadhodrond", "durin"])
                  (flatten [(with-prio "sshd" 2), "ovpn-tun0",
                            "CRON", "/USR/BIN/CRON" "postfix/master",
                            (with-prio "kernel" 5)]))
       )}
   {2 (bolt-spec {1 :shuffle}
                 (filter-interesting ["sshd", "kernel", "ovpn-tun0"]))
    3 (bolt-spec {2 ["program"]}
                 (incr-in-redis "program."))
    4 (bolt-spec {1 ["host"]}
                 (incr-in-redis "host."))
    }
    ))

;; The entry point of the module is the <code>run</code> function,
;; which, as the name implies, will run the topology desribed above on
;; a local cluster.
;;
;; It returns the running cluster object.
(defn run
  "Set up a local Storm cluster, and run our Viharnaplo topology."
  []
  (let [cluster (LocalCluster.)]
    (.submitTopology cluster "viharnaplo"
                     {
                      TOPOLOGY-DEBUG false
                      TOPOLOGY-WORKERS 8
                      }
                     (mk-topology))
    cluster))
