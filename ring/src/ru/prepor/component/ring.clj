(ns ru.prepor.component.ring
  (:require [com.stuartsierra.component :as component]))

(defprotocol RingHandler)

(defmacro defhandler
  [n dependencies handler-def]
  (let [record-name (symbol (format "%s*" n))
        ns (str *ns*)]
    `(do
       (defrecord ~record-name ~(conj dependencies '_handler)
         RingHandler
         component/Lifecycle
         (start [this#] (assoc this# :_handler ~handler-def))
         (stop [this#] this#)
         clojure.lang.IFn
         (invoke [_ req#] (~'_handler req#))
         (applyTo [this# args#] (clojure.lang.AFn/applyToHelper this# args#)))
       (def ~n
         (with-meta (~(symbol (format "map->%s*" n)) {})
           {:com.stuartsierra.component/dependencies ~(->> (map keyword dependencies)
                                                           (map (fn [x] [x x]))
                                                           (into {}))
            ::key (keyword ~ns ~(str n))})))))

(defrecord App [handler]
  component/Lifecycle
  (start [this]
    (let [handlers (->> (map val this)
                        (filter (partial satisfies? RingHandler)))]
      (assoc this :handler (fn [request] (some #(% request) handlers)))))
  (stop [this] this)
  clojure.lang.IFn
  (invoke [_ req] (handler req))
  (applyTo [this args] (clojure.lang.AFn/applyToHelper this args)))

(defn with-app
  [system key & handlers]
  (let [system-with-handlers (reduce (fn [system handler]
                                       (assoc system (-> handler meta ::key) handler))
                                     system handlers)
        handlers-as-dependencies (->> handlers
                                      (map meta)
                                      (map ::key)
                                      (map (fn [x] [x x]))
                                      (into {}))]
    (assoc system-with-handlers key
           (with-meta (map->App {})
             {:com.stuartsierra.component/dependencies handlers-as-dependencies}))))



