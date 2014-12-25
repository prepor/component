(ns ru.prepor.component.compojure
  (:require [ru.prepor.schema-compojure :as schema-comp]
            [ru.prepor.component.ring :as ring]
            [compojure.core :as comp]))

(defn with-matcher
  [system matcher]
  (assoc system :_matcher matcher))

(defmacro defroutes
  [n & args]
  (let [[options bindings & routes] (if (map? (first args))
                                      args (cons nil args))
        matcher (:matcher options)
        bindings (if matcher bindings (conj bindings '_matcher))]
    `(ring/defhandler ~n ~bindings
       (schema-comp/routes {:matcher ~(or matcher '_matcher)} ~@routes))))
