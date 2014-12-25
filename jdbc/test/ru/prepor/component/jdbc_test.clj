(ns ru.prepor.component.jdbc-test
  (:require [ru.prepor.component.jdbc :refer [new-jdbc]]
            [clojure.java.jdbc :as jdbc]
            [clojure.test :refer :all]
            [com.stuartsierra.component :as component]))

(deftest basic
  (let [jdbc (-> (new-jdbc {:classname "org.postgresql.Driver"
                            :subprotocol "postgresql"
                            :user "postgres"})
                 (component/start))]
    (try
      (is (= [{:test 1}] (jdbc/query jdbc ["SELECT 1 as test"])))
      (finally
        (component/stop jdbc)))))
