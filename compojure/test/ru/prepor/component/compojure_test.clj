(ns ru.prepor.component.compojure-test
  (:require [ru.prepor.component.compojure :refer [defroutes with-matcher]]
            [ru.prepor.schema-compojure :refer [context GET]]
            [ru.prepor.component.ring :refer [with-app]]
            [clojure.test :refer :all]
            [schema.coerce :as coerce]
            [schema.core :as s]
            [com.stuartsierra.component :as component]))

(defrecord Greeting [msg])

(defroutes HelloWorld [greeting]
  (GET "/" [] (str (:msg greeting) ", Anon"))
  (GET "/named" [[:params number :- s/Int]] (str (:msg greeting) ", " (* number 2))))


(deftest basic
  (let [s (-> (component/system-map
               :greeting (->Greeting "Hello"))
              (with-matcher coerce/string-coercion-matcher)
              (with-app :app HelloWorld)
              (component/start))]
    (is (= "Hello, Anon" (-> ((:app s) {:request-method :get
                                        :uri "/"})
                             :body)))

    (is (= "Hello, 4" (-> ((:app s) {:request-method :get
                                     :uri "/named"
                                     :params {:number "2"}})
                          :body)))))
