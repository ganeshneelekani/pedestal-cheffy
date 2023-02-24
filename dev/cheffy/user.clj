(ns cheffy.user
  (:require [cheffy.config :as c]
            [cheffy.server :as server]
            [com.stuartsierra.component :as component]
            [cheffy.routes :as r]
            [io.pedestal.http :as http]))


(defonce system-ref (atom nil))

;; (def terse-routes
;;   (route/expand-routes
;;    [[:cheffy :http "localhost.com"
;;      ["/recipes" {:get `list-recipes
;;                   :post `upsert-recipe}
;;       ["/:recipe-id" {:put [:update-recipe `upsert-recipe]}]]]]))


#_(defn start-dev []
    (let [cfg c/server-config]
      (reset! system-ref
              (-> (assoc cfg ::http/routes routes/table-routes)
                  (http/create-server)
                  (http/start))))
    :started)

(defn start-dev []
  (reset! system-ref
          (-> c/server-config
              (server/create-system)
              (component/start)))
  :started)

(defn stop-dev []
  (component/stop @system-ref)
  :stopped)

(defn restart-dev []
  (stop-dev)
  (start-dev)
  :restarted)

(comment

  (keys @system-ref)

  (start-dev)

  (restart-dev)

  (stop-dev))