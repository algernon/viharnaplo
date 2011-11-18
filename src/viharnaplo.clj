(ns viharnaplo
  "The main entry point of the Viharnaplo Storm application."
  (:use [viharnaplo.config])
  (:require [viharnaplo.storm :as viharnaplo]))

;; The main entry point, which just runs the LocalCluster forever.
;;
;; Usage:
;; <pre><code>$ lein run -m viharnaplo</code></pre>
(defn -main []
  (viharnaplo/run 0))
