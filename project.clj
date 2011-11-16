(defproject viharnaplo "0.0.0-SNAPSHOT"
  :description "Viharnaplo"
  :aot :all
  :dependencies [
                 [org.clojure/clojure "1.2.0"]
                 [storm "0.5.4"]
                 [accession "0.0.2"]
                 [compojure "0.6.4"]
                 [org.clojure/data.json "0.1.2"]
                 ]
  :dev-dependencies [
                     [lein-marginalia "0.6.0"]
                     [lein-ring "0.4.5"]
  ]
  :ring {:handler viharnaplo.web/app}
)
