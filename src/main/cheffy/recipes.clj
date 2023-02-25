(ns cheffy.recipes
  (:require [cheffy.interceptors :as interceptors]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [cheffy.db.recipe :as db]
            [ring.util.response :as rr]
            [io.pedestal.http :as http]
            [io.pedestal.http.body-params :as bp])
  (:import (java.util UUID)))

(def base-url "https://api.recipe.com")

(defn list-recipes-response
  [request]
  (let [db (get-in request [:system/database :database])
        account-id (get-in request [:headers "authorization"])
        recipes (db/list-all-recipes db account-id)]
    (rr/response recipes)))

(defn create-recipe-response [request]
  (let [recipe-id (str (UUID/randomUUID))
        uid (get-in request [:headers "authorization"])
        recipe (get-in request [:transit-params])
        conn (get-in request [:system/database :conn])]
    (db/insert-recipe! conn (assoc recipe :recipe-id recipe-id :uid uid))
    (rr/created (str base-url "/recipes/" recipe-id) {:recipe-id recipe-id})))

(def list-recipes
  [http/transit-body
   list-recipes-response])

(def create-recipe
  [(bp/body-params)
   http/transit-body
   create-recipe-response])

