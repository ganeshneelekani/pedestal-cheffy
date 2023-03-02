(ns cheffy.server
  (:require [com.stuartsierra.component :as component]
            [cheffy.config :as c]
            [cheffy.components.api-server :as api-server]
            [cheffy.components.auth :as auth])
  (:gen-class))

(defn create-system
  [config]
  (component/system-map
   :config config
   :auth (auth/service (:auth config))
   :database (api-server/database-service (:database config))
   :api-server (component/using
                (api-server/service (:service-map config))
                [:database :auth])))

(defn -main
  []
  (let [config c/server-config]
    (component/start (create-system config))))

