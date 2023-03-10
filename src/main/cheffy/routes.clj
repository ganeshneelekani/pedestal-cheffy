(ns cheffy.routes
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [cheffy.recipes :as recipes]
            [cheffy.account :as account]))

(defn list-recipes
  [request]
  {:status 200
   :body "list recipes"})

(defn upsert-recipe
  [request]
  {:status 200
   :body "upsert recipes"})

(defn routes
  []
  (route/expand-routes
   #{["/account/sign-up" :post account/sign-up :route-name :sign-up]
     ["/account/confirm" :post account/confirm :route-name :confirm]
     ["/account/log-in" :post account/log-in :route-name :log-in]
     ["/account/refresh" :post account/refresh-token :route-name :refresh-token]
     ["/account" :put account/update-role :route-name :update-role]
     ["/account" :delete account/delete-account :route-name :delete-role]

     ["/recipes" :get recipes/list-recipes :route-name :list-recipes]
     ["/recipes" :post recipes/create-recipe :route-name :create-recipe]
     ["/recipes/:recipe-id" :get recipes/retrieve-recipe :route-name :retrieve-recipe]
     ["/recipes/:recipe-id" :put recipes/create-recipe :route-name :update-recipe]
     ["/recipes/:recipe-id" :delete recipes/delete-recipe :route-name :delete-recipe]}))
