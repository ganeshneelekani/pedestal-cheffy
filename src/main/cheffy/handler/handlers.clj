(ns cheffy.handler.handlers
  (:require [cheffy.db.recipe :as db]
            [ring.util.response :as rr])
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


(defn retrieve-recipe-response
  [request]
  (let [db (get-in request [:system/database :database])
        recipe-id (-> request :path-params :recipe-id)
        recipe (db/find-recipe-by-id db recipe-id)]
    (if recipe
      (rr/response recipe)
      (rr/not-found {:type    "recipe-not-found"
                     :message "Recipe not found"
                     :data    (str "recipe-id " recipe-id)}))))

(defn delete-recipe-response
  [request]
  (let [recipe-id (get-in request [:path-params :recipe-id])
        conn (get-in request [:system/database :conn])
        deleted? (db/delete-recipe! conn {:recipe-id recipe-id})]
    (if deleted?
      (rr/status 204)
      (rr/not-found {:type    "recipe-not-found"
                     :message "Recipe not found"
                     :data    (str "recipe-id " recipe-id)}))))