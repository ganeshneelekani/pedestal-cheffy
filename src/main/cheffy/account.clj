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

(defn log-in-response
  [request]
  (let [cognito-log-in (auth/cognito-log-in
                        (:system/auth request)
                        (:transit-params request))]
    (rr/response cognito-log-in)))

(defn refresh-token-response
  [request]
  (let [{:keys [refresh-token]} (:transit-params request)
        sub (get-in request [:claims "sub"])
        cognito-refresh-token (auth/cognito-refresh-token
                               (:system/auth request)
                               {:refresh-token refresh-token :sub sub})]
    (rr/response cognito-refresh-token)))

(def log-in
  [http/transit-body
   (bp/body-params)
   log-in-response])

(defn update-role-response
  [request]
  (auth/cognito-update-role
   (:system/auth request)
   (:claims request))
  (rr/status 200))

(def update-role
  [(bp/body-params)
   http/transit-body
   interceptors/verify-json-web-token
   update-role-response])

(defn delete-user-response
  [request]
  (auth/cognito-delete-user
   (:system/auth request)
   (:claims request))
  (rr/status 200))

(def delete-account
  [(bp/body-params)
   http/transit-body
   interceptors/verify-json-web-token
   delete-user-response])

(defn refresh-token-response
  [request]
  (let [{:keys [refresh-token]} (:transit-params request)
        sub (get-in request [:claims "sub"])
        cognito-refresh-token (auth/cognito-refresh-token
                               (:system/auth request)
                               {:refresh-token refresh-token :sub sub})]
    (rr/response cognito-refresh-token)))

(def refresh-token
  [http/transit-body
   (bp/body-params)
   interceptors/verify-json-web-token
   refresh-token-response])