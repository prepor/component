(ns ru.prepor.component.ring-test
  (:require [ru.prepor.component.ring :refer [defhandler with-app]]
            [com.stuartsierra.component :as component]
            [clojure.test :refer :all]))

(defrecord Greeting [msg])

(defhandler HelloWorld [greeting]
  (fn [req]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str (:msg greeting) ", " (-> req :params :name))}))

(deftest basic
  (let [s (-> (component/system-map
               :greeting (->Greeting "Hello"))
              (with-app :app HelloWorld)
              (component/start))]
    (is (= "Hello, Andrew" (-> ((:app s) {:params {:name "Andrew"}}) :body)))))
