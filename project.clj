(defproject ru.prepor.component "0.1.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ru.prepor.component/ring "0.1.2"]
                 [ru.prepor.component/compojure "0.1.0"]
                 [ru.prepor.component/jdbc "0.1.1"]
                 [ru.prepor.component/httpkit "0.1.1"]]
  :plugins [[lein-sub "0.3.0"]]
  :sub ["ring" "jdbc" "compojure" "httpkit"])
