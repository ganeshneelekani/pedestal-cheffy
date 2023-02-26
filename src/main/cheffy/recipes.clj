(ns cheffy.recipes
  (:require
   [io.pedestal.http :as http]
   [io.pedestal.http.body-params :as bp]
   [cheffy.handler.handlers :as handlers])
  (:import (java.util UUID)))

(def list-recipes
  [http/transit-body
   handlers/list-recipes-response])

(def create-recipe
  [(bp/body-params)
   http/transit-body
   handlers/create-recipe-response])

(def retrieve-recipe
  [http/transit-body
   handlers/retrieve-recipe-response])

