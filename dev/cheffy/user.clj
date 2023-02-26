(ns cheffy.user
  (:require [cheffy.config :as c]
            [cheffy.server :as server]
            [com.stuartsierra.component :as component]
            [cheffy.routes :as r]
            [io.pedestal.http :as http]
            [com.stuartsierra.component.repl :as cr]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [io.pedestal.test :as pt]
            [cognitect.transit :as transit])
  (:import (java.io ByteArrayOutputStream ByteArrayInputStream)))


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

;; (defn start-dev []
;;   (reset! system-ref
;;           (-> c/server-config
;;               (server/create-system)
;;               (component/start)))
;;   :started)

;; (defn stop-dev []
;;   (component/stop @system-ref)
;;   :stopped)

(defn system [_]
  (-> c/server-config
      (server/create-system)))

(cr/set-init system)


(defn start-dev []
  (cr/start))

(defn stop-dev []
  (cr/stop))

(defn restart-dev []
  (cr/reset))

(defn restart-dev []
  (stop-dev)
  (start-dev)
  :restarted)

(defn transit-write [obj]
  (let [out (ByteArrayOutputStream.)
        writer (transit/writer out :json)]
    (transit/write writer obj)
    (.toString out)))

(defn transit-read [txt]
  (let [in (ByteArrayInputStream. (.getBytes txt))
        reader (transit/reader in :json)]
    (transit/read reader)))


(comment

  (start-dev)

  (restart-dev)

  (stop-dev)

  cr/system
  (-> cr/system :api-server :service ::http/service-fn)

  (-> cr/system :database :database)

  (pt/response-for
   (-> cr/system :api-server :service ::http/service-fn)
   :get  "/recipes"
   :headers {"Authorization" "auth0|63f3527a09b12c77b8d383b6"})

  (defn cheffy-interceptors
    [service-map sys-interceptors]
    (let [service-map (-> cr/system :api-server :service)
          default-interceptors (-> service-map
                                   (http/default-interceptors)
                                   ::http/interceptors)
          interceptors (into [] (concat
                                 (butlast default-interceptors)
                                 sys-interceptors
                                 [(last default-interceptors)]))]
      (assoc service-map ::http/interceptors interceptors)))

  (time
   (with-open [conn (jdbc/get-connection (-> cr/system :database))]
     {:public (sql/find-by-keys conn :recipe {:public true})
      :drafts (sql/find-by-keys conn :recipe {:public false
                                              :uid    "auth0|5ef440986e8fbb001355fd9c"})}))

  (-> (transit-write {:name "name"
                      :public true
                      :prep-time 30
                      :img "https://github.com/clojure.png"})
      (transit-read))


  (pt/response-for
   (-> cr/system :api-server :service ::http/service-fn)
   :post "/recipes"
   :headers {"Authorization" "auth0|63f3527a09b12c77b8d383b6"
             "Content-Type" "application/transit+json"}
   :body (transit-write {:name "name"
                         :public true
                         :prep-time 30
                         :img "https://github.com/clojure.png"}))

  (pt/response-for
   (-> cr/system :api-server :service ::http/service-fn)
   :post "/recipes"
   :headers {"Authorization" "auth0|63f3527a09b12c77b8d383b6"
             "Content-Type" "application/transit+json"}
   :body (transit-write {:name "name"
                         :public true
                         :prep-time 30
                         :img "https://github.com/clojure.png"}))

  (pt/response-for
   (-> cr/system :api-server :service ::http/service-fn)
   :get "/recipes/a1995316-80ea-4a98-939d-7c6295e4bb46"
   :headers {"Authorization" "auth0|63f3527a09b12c77b8d383b6"
             "Content-Type" "application/transit+json"}
    ;; a1995316-80ea-4a98-939d-7c6295e4bb46)
   )

  (sql/insert! (-> cr/system :database :database) :recipe {:recipe_id "asa"
                                                           :uid "auth0|63f3527a09b12c77b8d383b6"
                                                           :name "name"
                                                           :public true
                                                           :prep_time 30
                                                           :img "https://github.com/clojure.png"})

  (start-dev)

  (restart-dev)

  (stop-dev))