(ns cheffy.interceptors
  (:require [io.pedestal.interceptor :as interceptor]
            [cheffy.db.recipe :as db]
            [ring.util.response :as rr]))

(def base-url "https://api.recipe.com")

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

(def recipe-interceptor
  (interceptor/interceptor
   {:name ::recipe-interceptor
    :enter (fn [ctx]
             (let [path-recipe-id (get-in ctx [:request :path-params :recipe-id])
                   recipe-id (or (when path-recipe-id (parse-uuid path-recipe-id))
                                 (random-uuid))
                   uid (get-in ctx [:request :headers "authorization"])
                   recipe (get-in ctx [:request :transit-params])
                   conn (get-in ctx [:request :system/database :conn])]
               (db/insert-recipe! conn (assoc recipe :recipe-id recipe-id :uid uid))
               (rr/created (str base-url "/recipes/" recipe-id) {:recipe-id recipe-id})))}))
