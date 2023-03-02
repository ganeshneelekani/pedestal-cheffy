(ns cheffy.components.auth
  (:require [com.stuartsierra.component :as component]
            [cognitect.aws.client.api :as aws]
            [com.stuartsierra.component.repl :as cr]
            [clojure.data.json :as json])
  (:import (javax.crypto.spec SecretKeySpec)
           (javax.crypto Mac)
           (java.util Base64)
           (com.auth0.jwk UrlJwkProvider GuavaCachedJwkProvider)
           (com.auth0.jwt.interfaces RSAKeyProvider)
           (com.auth0.jwt.algorithms Algorithm)
           (com.auth0.jwt JWT)))

(def token {:ChallengeParameters {},
            :AuthenticationResult {:AccessToken "eyJraWQiOiI5eUZFVDZ2enA2c0dvWlRCZTRtZFl0MzRxcHF5WXNsclVmb3dET3Vsc3ZnPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiI5Njc1OWM3Mi03NDExLTQwZTctOGYxZS0yN2NiYmE3ZTRmZTYiLCJpc3MiOiJodHRwczpcL1wvY29nbml0by1pZHAudXMtZWFzdC0xLmFtYXpvbmF3cy5jb21cL3VzLWVhc3QtMV92aGZaMzNqbmUiLCJjbGllbnRfaWQiOiI2YjdvNTRsMDE1ZmM3ZjJicm4wdWg2aWJyIiwib3JpZ2luX2p0aSI6IjNjNWRlMmQzLTBlZTctNGRkYi1hMmVlLWQ0MmQ4ZDNmZDUzNyIsImV2ZW50X2lkIjoiMDVhODVkYjYtYTVkNS00YWVlLTkyMTEtNjI3Y2QxNjEyZjkzIiwidG9rZW5fdXNlIjoiYWNjZXNzIiwic2NvcGUiOiJhd3MuY29nbml0by5zaWduaW4udXNlci5hZG1pbiIsImF1dGhfdGltZSI6MTY3ODAwMjM2NywiZXhwIjoxNjc4MDA1OTY3LCJpYXQiOjE2NzgwMDIzNjcsImp0aSI6ImVhNzhkYjE5LWZiMTgtNDFmZC04ZTVkLTViMjRlMjg0ZGQ2MSIsInVzZXJuYW1lIjoiOTY3NTljNzItNzQxMS00MGU3LThmMWUtMjdjYmJhN2U0ZmU2In0.AWVX4qA7qsXaV9cKiOxVjvkGEGEGcoOp6A8x26N257sflxQcOoitfMKek1sry30oM8J48pk9mb-wR5hSVOIShU34Q7YjQCJ7B7EHflt_G0nmLpRXouLRysVqeKFjF99RdTIqt-1eo4vohV-Uvsmm_lYHjeM_kR7qs7TeG2WqUXTYXf9MX_4zZC9j9xtfRXarJ0rvlDjySSXhVBNn0RGkcAJcd_39ks4eLD7ee3tkomQ4lW3og2-LClzTpAHZDLNaK6A4N10V0d_btx84mJNNJT0f98TZzYUZDzgGDllD4cUaCeq_ONVO6F7k0fr-1efJjwYmCl1LdqY4Y0UI2yvIlQ",
                                   :ExpiresIn 3600,
                                   :TokenType "Bearer",
                                   :RefreshToken "eyJjdHkiOiJKV1QiLCJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiUlNBLU9BRVAifQ.Z_0T5sMRol22BHTqXJD2mPIuf1j__n2utLznnFELS0cDqrbIuXnQLWY3zRakOA2pZzLQtwto62L74CrnwNuFhssSUagKsxzixruGYVQBxjPNBrs6RmriK8HPWRWeNBCFk8byifbH4Jq3CvlivydlZ1DTyJ9m5ZqdJIOgtHDv3u3cnZTwP4-mpUDR2WxwynTH5wpj3qwVKHjQ-yI-BpKfwYTfCxjP8B0UbjkDkCl5_BKhYizHhqQcpm57IXjOPdMnMyxcgI03Uviq9aVizZZgi97RH1wqkZPMz6UNTJE1XdmTa1sqQRyhy8d1vb9iD_U0NIqELRuOT4PIgkSZXc_DzQ.V2aRc25kEoBvRNMx.lwz1R8q3gjIwFPhp37j2pJ0y3G07i9x4V-dUg88AiEf2h667n-Oa2B5MXABYI3Rajk3GkXDWMiyDn2QW1FSfP4UTQ-wa8hrP0ErSdDcveHIQCuvD9nM7NxrUzpqI9CAgkiZCbGI3_Irib-G3I1vpaUBqVJ4wxiJahAcL2GJzt0c1chRHqoIebWdrrTsDx06OCskOgBzLQelxWNbdEWzyr9RQnBlvOONrGidXQ2_gzHuSAMRNGsaJ2RuFCPykNCHq-JyQkB9VKCnem0QzJDShtvN8yTomFvFz98SZYvSl95c-yzQBEA263d25yIkVJMUr3N2N3jpVK-igoe6mQ2lbYJcgyO_HYzF3_6lPxmce9gQyezbdSprsXeSm0ivGpJEKFHaM6_1_ir-N4e3RGRXFudgkiExV7jbMlytJWfChKluG2sYP5fvzukFIcWvMnuFKndxmuhSijp6jHrHhZBKG5Ba8xGb-CU0QXRqjCU3t_EGqnRaT3H-Hc37SmDr3Effs9v_C6GwpRUIhwLUrQULfWGghC_ILTG-ixGsU06wiymFD5eiC_ytrT8oMcM6vFa12gkWB3XeeOJ0qqJjtY8LiOy5dxW9saqM03_KLPKF3qCpqDp25HMOxQ5AqOmAeZYODnhd18424k9EDBOJAdKjMuR8q5abrwq3bQhN6CTHplWdb5WUHLLb0EbEDQ8V7PQg0IjBwbQmo9mCcgU73FKLWxQsIXx7aIvXq_tmICdkn-a2QjzOJS9ozBwlodR8FaHQhca4rag1KthzxWpxMkNGoUrgzwfYehRbSO1YCva6qdmFNeanUpakbNBVe1De_Rs3xKfEZcAsNlS9-R9b0V3Y8kg-ShOfRxcLGQmWAKXxU4-lPMqRCtyYBs1iNkAtF38dY8PxMoTKH_9QE9qGgJncndSDLt0cN2KWWZWpXJem8mC3uz_Kr7KS5b3fIJJYw1EdqBYKGESEQLRV7iEHnSK3nWj7c3KOyPgR_etDgyKUc2VmZ01Et8QysfVJuryrYomc1mJ5mG0wnAp-9EpxfTKIswjWLVLXr9bmWEKtD4M54bFyqHdHbrPEIc5njDCGmtCp0qblYjsRyjca1nNQxpgYL8ZTDy_ro_x4tojfN_hY7VEyg81kxvsXr5uubACMEJMzVnJkFKxmSrWSqRiOPEfTkTkUqwgV-WZU6fVyfAIK3XuDn5cDepIFu3l-m-Pyg20UOWvkTOQmLhOckCPXK9CYfOW2cRwub1VIYYFelKoPQSqig8gl1h4lmbUtiU6tUEFM8aABowPQi0NX-7ZhIPZ7bXBbpk69uIJW8xqCUFrfpO3scCAwv7QPZFQdC.STgOgbj90QYRcP7mx4IrrQ", :IdToken "eyJraWQiOiJGOEl3Qm1KeEZNRGVSWlpuaWlZQ00zQkFBWm0rVVR0ZEc0djB1Q3BLS2pVPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiI5Njc1OWM3Mi03NDExLTQwZTctOGYxZS0yN2NiYmE3ZTRmZTYiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiaXNzIjoiaHR0cHM6XC9cL2NvZ25pdG8taWRwLnVzLWVhc3QtMS5hbWF6b25hd3MuY29tXC91cy1lYXN0LTFfdmhmWjMzam5lIiwiY29nbml0bzp1c2VybmFtZSI6Ijk2NzU5YzcyLTc0MTEtNDBlNy04ZjFlLTI3Y2JiYTdlNGZlNiIsIm9yaWdpbl9qdGkiOiIzYzVkZTJkMy0wZWU3LTRkZGItYTJlZS1kNDJkOGQzZmQ1MzciLCJhdWQiOiI2YjdvNTRsMDE1ZmM3ZjJicm4wdWg2aWJyIiwiZXZlbnRfaWQiOiIwNWE4NWRiNi1hNWQ1LTRhZWUtOTIxMS02MjdjZDE2MTJmOTMiLCJ0b2tlbl91c2UiOiJpZCIsImF1dGhfdGltZSI6MTY3ODAwMjM2NywiZXhwIjoxNjc4MDA1OTY3LCJpYXQiOjE2NzgwMDIzNjcsImp0aSI6IjMwMTIwMGFkLTBiNGMtNDEwMy05MTkyLTBhNWUzM2E5YzI3NiIsImVtYWlsIjoiZ2FuZXNobmVlbGVrYW5pMDdAZ21haWwuY29tIn0.r9cD4ZljkgQbtXh5fL1xGnf19gXfxRNvJKTpDLxIzWRnL6MWe0zPnBQ68LRfV-I739xIDdnmG3lbN6h3hL7ox3WGF_5a-r3_rtsJa1cIcG6CrERpl-kYKmE8P0F1n5LNfEXJGD13fj2MQERMm70PFv-mTzpkLqpdyw8iz5DLgIniocLddjvY_9-uyNBmzvtjuLFdCxicHaPhvJhPi0_nKHpXkb9YBP161nHRrQFd7lJl09m3mIxuMD36ONE8v0npRgo_48NfrCAksDVeJgw3IJdT8ey1EeJNQX-pwI61S3yGEyxDhIvzndnqleVd2pBKpXq_2WxyYJAi0425d9n8YA"}})

(defn validate-signature
  [{:keys [key-provider]} token]
  (let [algorithm (Algorithm/RSA256 key-provider)
        verifier (.build (JWT/require algorithm))
        verified-token (.verify verifier token)]
    (.getPayload verified-token)))

(defn decode-to-str
  [s]
  (String. (.decode (Base64/getUrlDecoder) s)))

(defn decode-token
  [token]
  (-> token
      (decode-to-str)
      (json/read-str)))

(defn verify-payload
  [{:keys [config]} {:strs [client_id iss token_use] :as payload}]
  (when-not
   (and
    (= (:client-id config) client_id)
    (= (:jwks config) iss)
    (contains? #{"access" "id"} token_use))
    (throw (ex-info "Token verification failed" {})))
  payload)

(defn verify-and-get-payload
  [auth token]
  (->> token
       (validate-signature auth)
       (decode-token)
       (verify-payload auth)))

(comment

  (validate-signature
   (:auth cr/system)
   (-> token :AuthenticationResult :AccessToken))

  (-> (validate-signature
       (:auth cr/system)
       (-> token :AuthenticationResult :AccessToken))
      (decode-token))

  (verify-and-get-payload
   (:auth cr/system)
   (-> token :AuthenticationResult :AccessToken)))




(defn calculate-secret-hash
  [{:keys [client-id client-secret username]}]
  (let [hmac-sha256-algorithm "HmacSHA256"
        signing-key (SecretKeySpec. (.getBytes client-secret) hmac-sha256-algorithm)
        mac (doto (Mac/getInstance hmac-sha256-algorithm)
              (.init signing-key)
              (.update (.getBytes username)))
        raw-hmac (.doFinal mac (.getBytes client-id))]
    (.encodeToString (Base64/getEncoder) raw-hmac)))

(defn when-anomaly-throw
  [result]
  (when (contains? result :cognitect.anomalies/category)
    (throw (ex-info (:__type result) result))))

(defn create-cognito-account
  [{:keys [config cognito-idp]} {:keys [email password]}]
  (let [client-id (:client-id config)
        client-secret (:client-secret config)
        result (aws/invoke cognito-idp
                           {:op :SignUp
                            :request {:ClientId client-id
                                      :Username email
                                      :Password password
                                      :SecretHash (calculate-secret-hash
                                                   {:client-id client-id
                                                    :client-secret client-secret
                                                    :username email})}})]
    (when-anomaly-throw result)
    [{:account/account-id (:UserSub result)
      :account/display-name email}]))

(defn confirm-cognito-account
  [{:keys [config cognito-idp]} {:keys [email confirmation-code]}]
  (let [client-id (:client-id config)
        client-secret (:client-secret config)
        result (aws/invoke cognito-idp
                           {:op :ConfirmSignUp
                            :request
                            {:ClientId client-id
                             :Username email
                             :ConfirmationCode confirmation-code
                             :SecretHash (calculate-secret-hash
                                          {:client-id client-id
                                           :client-secret client-secret
                                           :username email})}})]
    (when-anomaly-throw result)))

(defn cognito-update-role
  [{:keys [cognito-idp config]} claims]
  (let [client-id (:client-id config)
        client-secret (:client-secret config)
        user-pool-id (:user-pool-id config)
        {:strs [sub]} claims
        result (aws/invoke cognito-idp
                           {:op :AdminAddUserToGroup
                            :request
                            {:ClientId client-id
                             :UserPoolId user-pool-id
                             :Username sub
                             :GroupName "cheffs"
                             :SecretHash (calculate-secret-hash
                                          {:client-id client-id
                                           :client-secret client-secret
                                           :username sub})}})]
    (when-anomaly-throw result)
    result))

(defn cognito-delete-user
  [{:keys [cognito-idp config]} claims]
  (let [user-pool-id (:user-pool-id config)
        {:strs [sub]} claims
        result (aws/invoke cognito-idp
                           {:op :AdminDeleteUser
                            :request
                            {:UserPoolId user-pool-id
                             :Username sub}})]
    (when-anomaly-throw result)))


(defrecord Auth [config cognito-idp]

  component/Lifecycle

  (start [component]
    (println ";; Starting Auth")
    (let [key-provider (-> (:jwks config)
                           (UrlJwkProvider.)
                           (GuavaCachedJwkProvider.))]
      (assoc component
             :cognito-idp (aws/client {:api :cognito-idp})
             :key-provider (reify RSAKeyProvider
                             (getPublicKeyById [_ kid]
                               (.getPublicKey (.get key-provider kid)))

                             (getPrivateKey [_]
                               nil)

                             (getPrivateKeyId [_]
                               nil)))))

  (stop [component]
    (println ";; Stopping Auth")
    (assoc component
           :cognito-idp nil)))

(defn service
  [config]
  (map->Auth {:config config}))

(defn cognito-log-in
  [{:keys [config cognito-idp]} {:keys [email password]}]
  (let [client-id (:client-id config)
        client-secret (:client-secret config)
        user-pool-id (:user-pool-id config)
        result (aws/invoke cognito-idp
                           {:op :AdminInitiateAuth
                            :request
                            {:ClientId client-id
                             :UserPoolId user-pool-id
                             :AuthFlow "ADMIN_USER_PASSWORD_AUTH"
                             :AuthParameters {"USERNAME" email
                                              "PASSWORD" password
                                              "SECRET_HASH" (calculate-secret-hash
                                                             {:client-id client-id
                                                              :client-secret client-secret
                                                              :username email})}}})]
    (when-anomaly-throw result)
    (:AuthenticationResult result)))

(defn cognito-refresh-token
  [{:keys [config cognito-idp]} {:keys [refresh-token sub]}]
  (let [client-id (:client-id config)
        client-secret (:client-secret config)
        user-pool-id (:user-pool-id config)
        result (aws/invoke cognito-idp
                           {:op :AdminInitiateAuth
                            :request
                            {:ClientId client-id
                             :UserPoolId user-pool-id
                             :AuthFlow "REFRESH_TOKEN_AUTH"
                             :AuthParameters {"REFRESH_TOKEN" refresh-token
                                              "SECRET_HASH" (calculate-secret-hash
                                                             {:client-id client-id
                                                              :client-secret client-secret
                                                              :username sub})}}})]
    (when-anomaly-throw result)
    (:AuthenticationResult result)))


