(ns cheffy.routes
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [cheffy.recipes :as recipes]))

(defn list-recipes
  [request]
  {:status 200
   :body "list recipes"})

(defn upsert-recipe
  [request]
  {:status 200
   :body "upsert recipes"})

(def routes
  (route/expand-routes
   #{["/recipes" :get recipes/list-recipes :route-name :list-recipes]
     ["/recipes" :post recipes/create-recipe :route-name :create-recipe]
     ["/recipes/:recipe-id" :put recipes/create-recipe :route-name :update-recipe]}))
