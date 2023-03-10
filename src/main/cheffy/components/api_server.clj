(ns cheffy.components.api-server
  (:require [com.stuartsierra.component :as component]
            [io.pedestal.http :as http]
            [cheffy.routes :as routes]
            [next.jdbc :as jdbc]
            [io.pedestal.interceptor :as interceptor]
            [clojure.pprint :as pp]))

(defn dev?
  [service-map]
  (= :dev (:env service-map)))

(defn cheffy-routes
  [service-map]
  (let [routes (if (dev? service-map)
                 #(routes/routes)
                 (routes/routes))]
    (assoc service-map ::http/routes routes)))

(defn inject-system
  [system]
  (interceptor/interceptor
   {:name ::inject-system
    :enter (fn [ctx]
             (update-in ctx [:request] merge system))}))

(defn create-cheffy-server
  [service-map]
  (http/create-server (if (dev? service-map)
                        (http/dev-interceptors service-map)
                        service-map)))

(defn cheffy-interceptors
  [service-map sys-interceptors]
  (let [default-interceptors (-> service-map
                                 (http/default-interceptors)
                                 ::http/interceptors)
        interceptors (into [] (concat
                               (butlast default-interceptors)
                               sys-interceptors
                               [(last default-interceptors)]))]
    (assoc service-map ::http/interceptors interceptors)))


(defrecord ApiServer [service-map service database auth]
  component/Lifecycle

  (start [component]
    (println ";; Stating API Server ")
    (let [service (-> service-map
                      (cheffy-routes)
                      (cheffy-interceptors [(inject-system {:system/database database :system/auth auth})])
                      (create-cheffy-server)
                      (http/start))]
      (assoc component :service service)))

  (stop [component]
    (println ";; Stopping API server")
    (when service
      (http/stop service))
    (assoc component :service nil)))

(defn service
  [service-map]
  (map->ApiServer {:service-map service-map}))

(defrecord Database [database conn]

  component/Lifecycle

  (start [component]
    (println ";; Starting Database")
    (let [conn (jdbc/with-options database jdbc/snake-kebab-opts)]
      (assoc component :conn conn)))

  (stop [component]
    (println ";; Stopping Database")
    (assoc component :conn nil)))


(defn database-service
  [config]
  (map->Database {:database config}))