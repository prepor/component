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


(require '[ru.prepor.component.compojure :refer [defroutes with-matcher]]
         '[ru.prepor.schema-compojure :refer [context GET]]
         '[ru.prepor.component.ring :refer [with-app]]
         '[com.stuartsierra.component :as component]
         '[schema.coerce :as coerce]
         '[schema.core :as s])

(defrecord Greeting [msg])

(defrecord DB [connection])

(defroutes Api [greeting]
  (context "/users" []
    (context "/:id" [[:params id :- s/Int]]
      (GET "/" [] (format "%s, %s (odd?: %s)" (:msg greeting) id (odd? id)))))
  (GET "/" [] (format "%s, world!" (:msg greeting))))

(defroutes Admin [db]
  (context "/admin" []
    (GET "/connections" [] (format "Connection status: %s" (:connection db)))))

(def system (-> (component/system-map
                 :greeting (->Greeting "Hello")
                 :db (->DB "good connection"))
                (with-matcher coerce/string-coercion-matcher)
                (with-app :app Api Admin)
                (component/start)))

((:app system) {:request-method :get, :uri "/"})
;; => {:status 200, :headers {"Content-Type" "text/html; charset=utf-8"}, :body "Hello, world!"}

((:app system) {:request-method :get, :uri "/users/5"})
;; => {:status 200, :headers {"Content-Type" "text/html; charset=utf-8"}, :body "Hello, 5 (odd?: true)"}

((:app system) {:request-method :get, :uri "/admin/connections"})
;; => {:status 200, :headers {"Content-Type" "text/html; charset=utf-8"}, :body "Connection status: good connection"}

(defn go!
  []
  (let [s (-> (component/system-map
               :greeting (->Greeting "Hello")
               :db (->DB "good connection"))
              (with-matcher coerce/string-coercion-matcher)
              (with-app :app Api Admin)
              (component/start))]
    (prn (-> ((:app s) {:request-method :get
                        :uri "/"})
             :body))

    (prn (-> ((:app s) {:request-method :get
                        :uri "/users/5"})
             :body))

    (prn (-> ((:app s) {:request-method :get
                        :uri "/admin/connections"})
             :body))))

(require '[ru.prepor.component.httpkit :refer [new-http-server]])

(def system (-> (component/system-map
                 :greeting (->Greeting "Hello")
                 :db (->DB "good connection")
                 :server (new-http-server {:port 8000}))
                (with-matcher coerce/string-coercion-matcher)
                (with-app :app Api Admin)
                (component/system-using {:server [:app]})
                (component/start)))
