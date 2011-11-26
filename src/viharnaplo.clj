(ns viharnaplo
  "The main entry point of the Viharnaplo Storm application."
  (:use [viharnaplo.config])
  (:require [viharnaplo.storm :as viharnaplo]))

;; The main entry point, which just runs the LocalCluster forever,
;; unless an argument is given. In that case, it runs the cluster for
;; the specified amount of seconds.
;;
;; Usage:
;; <pre><code>$ lein run -m viharnaplo</code></pre>
;; Or
;; <pre><code>$ lein run -m viharnaplo 10</code></pre>
(defn -main [& args]
  (let [cluster (viharnaplo/run)]
    (if (not (empty? args))
      (do
        (Thread/sleep (* 1000 (Integer/parseInt (first args))))
        (.shutdown cluster)))))
