(ns cheffy.interceptors
  (:require [io.pedestal.interceptor :as interceptor]))


(comment (time
          (with-open [conn (jdbc/get-connection (-> cr/system))]
            {:public (sql/find-by-keys conn :recipe {:public true})
             :drafts (sql/find-by-keys conn :recipe {:public false :uid  "auth0|5ef440986e8fbb001355fd9c"})})))

(def db-interceptor
  (interceptor/interceptor
   {:name ::db-interceptor
    :enter (fn [ctx]
             (let [conn (get-in ctx [:request :system/database :conn])
                   db (get-in ctx [:request :system/database :database])]
               (update-in ctx [:request :system/database] assoc :db db)))}))

(def recipe-interceptor
  (interceptor/interceptor
   {:name ::recipe-interceptor
    :enter (fn [ctx])}))

