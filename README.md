# ru.prepor.component

[![Travis status](https://secure.travis-ci.org/prepor/component.png)](http://travis-ci.org/prepor/component)

This is a collection of small utilities for integration different libraries with [component](https://github.com/stuartsierra/component).

[![Clojars Project](http://clojars.org/ru.prepor.component/latest-version.svg)](http://clojars.org/ru.prepor.component)
[![Clojars Project](http://clojars.org/ru.prepor.component/ring/latest-version.svg)](http://clojars.org/ru.prepor.component/ring)
[![Clojars Project](http://clojars.org/ru.prepor.component/jdbc/latest-version.svg)](http://clojars.org/ru.prepor.component/jdbc)
[![Clojars Project](http://clojars.org/ru.prepor.component/compojure/latest-version.svg)](http://clojars.org/ru.prepor.component/compojure)
[![Clojars Project](http://clojars.org/ru.prepor.component/httpkit/latest-version.svg)](http://clojars.org/ru.prepor.component/httpkit)


## Usage

### ring

Ring handlers can be defined as components with attached dependencies information.

```clojure

(require '[ru.prepor.component.ring :refer [defhandler with-app]]
         '[com.stuartsierra.component :as component])

(defrecord Greeting [msg])

(defhandler HelloWorld [greeting]
  (fn [req]
    (when (= "/" (:uri req))
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (str (:msg greeting) ", " (-> req :params :name))})))
```

After starting this component can be used as usual ring handler.

```clojure
(def handler (-> (assoc HelloWorld :greeting (->Greeting "Hello"))
                 (component/start )))

(handler {:request-method :get
          :uri "/"
          :params {:name "Andrew"}})
;; => {:status 200, :headers {"Content-Type" "text/html"}, :body "Hello, Andrew"}
```

Let's define one more component

```clojure
(defrecord Master [name])

(defhandler API [greeting master]
  (fn [req]
    (when (= "/master" (:uri req))
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (str (:msg greeting) ", " (:name master))})))
```

Components can be combined to new component and attached to system-map before system started.

```clojure
(def system (-> (component/system-map
                 :greeting (->Greeting "Hello")
                 :master (->Master "Ivan"))
                (with-app :app API HelloWorld)
                (component/start)))
```

`with-app` takes keyword in system map for attaching and list of ring components. They will be called one by one until one returns something. And again, it can be used as usual ring handler.

```clojure
((:app system) {:request-method :get
                :uri "/"
                :params {:name "Andrew"}})
;; => {:status 200, :headers {"Content-Type" "text/html"}, :body "Hello, Andrew"}

((:app system) {:request-method :get
                :uri "/master"})
;; => {:status 200, :headers {"Content-Type" "text/html"}, :body "Hello, Ivan"}
```

### compojure

Actually this is not utility for [compojure](https://github.com/weavejester/compojure), but for [schema-compojure](https://github.com/prepor/schema-compojure) :)

Compojure handlers are just ring handlers, you're not surprised, aren't you?

```clojure
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
```

Matcher can be defined for individual handler, routes or as dependency for entire system.

```clojure
(def system (-> (component/system-map
                 :greeting (->Greeting "Hello")
                 :db (->DB "good connection"))
                (with-matcher coerce/string-coercion-matcher)
                (with-app :app Api Admin)
                (component/start)))
                ```

Use it!

```clojure
((:app system) {:request-method :get, :uri "/"})
;; => {:status 200, :headers {"Content-Type" "text/html; charset=utf-8"}, :body "Hello, world!"}

((:app system) {:request-method :get, :uri "/users/5"})
;; => {:status 200, :headers {"Content-Type" "text/html; charset=utf-8"}, :body "Hello, 5 (odd?: true)"}

((:app system) {:request-method :get, :uri "/admin/connections"})
;; => {:status 200, :headers {"Content-Type" "text/html; charset=utf-8"}, :body "Connection status: good connection"}
```

### http-kit

Now we can start ring handler from previous examples via http-kit

```clojure
(require '[ru.prepor.component.httpkit :refer [new-http-server]])

(def system (-> (component/system-map
                 :greeting (->Greeting "Hello")
                 :db (->DB "good connection")
                 :server (new-http-server {:port 8000}))
                (with-matcher coerce/string-coercion-matcher)
                (with-app :app Api Admin)
                (component/system-using {:server [:app]})
                (component/start)))
```

### jdbc

JDBC utility combines component, jdbc and c3p0.

```clojure
(require '[ru.prepor.component.jdbc :refer [new-jdbc]]
         '[clojure.java.jdbc :as jdbc]
         '[com.stuartsierra.component :as component])

(def jdbc (-> (new-jdbc {:classname "org.postgresql.Driver"
                         :subprotocol "postgresql"
                         :user "postgres"})
              (component/start)))

(jdbc/query jdbc ["SELECT 1 as test"])
;; => ({:test 1})
```

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
