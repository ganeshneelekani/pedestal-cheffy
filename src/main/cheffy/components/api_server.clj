(ns cheffy.components.api-server
  (:require [com.stuartsierra.component :as component]
            [io.pedestal.http :as http]
            [cheffy.routes :as r]))

(defrecord ApiServer [service-map service]
  component/Lifecycle

  (start [component]
    (println "Starting API server 1" service "  " service-map)
    (let [service (-> service-map
                      (assoc ::http/routes r/routes)
                      (http/create-server)
                      (http/start))
          _ (println "----P---" service)]
      (assoc component :service service)))

  (stop [component]
    (println ";; Stopping API server")
    (when service
      (http/stop service))
    (assoc component :service nil)))


(defn service
  [service-map]
  (map->ApiServer {:service-map service-map}))