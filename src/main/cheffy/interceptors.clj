(ns cheffy.interceptors
  (:require [io.pedestal.interceptor :as interceptor]
            [cheffy.db.recipe :as db]
            [ring.util.response :as rr]
            [cheffy.components.auth :as auth]
            [next.jdbc.sql :as sql]))

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

(def sign-up-interceptor
  {:name ::sign-up-interceptor
   :enter (fn [{:keys [request] :as ctx}]
            (let [create-cognito-account (auth/create-cognito-account
                                          (:system/auth request)
                                          (:transit-params request))]
              (assoc ctx :tx-data create-cognito-account)))
   :leave (fn [ctx]
            (let [account-id (-> ctx :tx-data (first) :account/account-id)]
              (assoc ctx :response (rr/response {:account-id account-id}))))})


(def transact-interceptor
  (interceptor/interceptor
   {:name ::transact-interceptor
    :enter (fn [ctx]
             (let [conn (get-in ctx [:request :system/database :conn])
                   tx-data (get ctx :tx-data)]
               (assoc ctx :tx-result (sql/insert! conn :account tx-data))))}))

(def confirm-account-interceptor
  {:name ::confirm-account-interceptor
   :enter (fn [{:keys [request] :as ctx}]
            (auth/confirm-cognito-account
             (:system/auth request)
             (:transit-params request))
            ctx)
   :leave (fn [ctx]
            (assoc ctx :response (rr/status 204)))})

(def query-interceptor
  (interceptor/interceptor
   {:name ::query-interceptor
    :enter (fn [ctx]
             (let [q-data (get ctx :q-data)]
               (assoc ctx :q-result (d/q q-data))))}))

