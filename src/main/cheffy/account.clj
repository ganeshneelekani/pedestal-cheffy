(ns cheffy.account
  (:require [cheffy.components.auth :as auth]
            [ring.util.response :as rr]
            [io.pedestal.http :as http]
            [io.pedestal.http.body-params :as bp]
            [cheffy.interceptors :as interceptors]
            [io.pedestal.interceptor.chain :as chain]))

(def sign-up
  [http/transit-body
   (bp/body-params)
   interceptors/sign-up-interceptor
   interceptors/transact-interceptor])

(def confirm
  [http/transit-body
   (bp/body-params)
   interceptors/confirm-account-interceptor])
