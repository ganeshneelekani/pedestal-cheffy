(ns cheffy.server
  (:require [com.stuartsierra.component :as component]
            [cheffy.config :as c]
            [cheffy.components.api-server :as api-server])
  (:gen-class))

(defn create-system
  [config]
  (component/system-map
   :config config
   :api-server (api-server/service (:service-map config))))

(defn -main
  []
  (let [config c/server-config]
    (component/start (create-system config))))

