(ns ru.prepor.component.httpkit
  (:require [com.stuartsierra.component :as component]
            [org.httpkit.server :as http]))

(defrecord HttpServer [app server options]
  component/Lifecycle
  (start [this]
    (assoc this :server (http/run-server app options)))
  (stop [this]
    (server :timeout (:timeout options 100))
    this))

(defn new-http-server
  [options]
  (map->HttpServer {:options options}))
