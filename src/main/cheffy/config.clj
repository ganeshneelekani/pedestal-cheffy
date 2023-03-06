(ns cheffy.config)

(def database {:dbname   (or (System/getenv "DB_DBNAME") "it_data")
               :user     (or (System/getenv "DB_USER") "myuser")
               :password (or (System/getenv "DB_PASSWORD") "mypassword")
               :host     (or (System/getenv "DB_HOST") "localhost")
               :port     (or (System/getenv "DB_PORT") 5432)
               :dbtype   "postgresql"})

(def auth-config {:client-id (or (System/getenv "AUTH_CLIENT_ID") "6b7o54l015fc7f2brn0uh6ibr")
                  :client-secret (or (System/getenv "AUTH_CLIENT_SECREAT") "Masked")
                  :user-pool-id (or (System/getenv "AUTH_POOL_ID") "us-east-1_vhfZ33jne")
                  :jwks "Masked"})

(def service-map {:env :dev
                  :io.pedestal.http/type :jetty
                  :io.pedestal.http/join? false
                  :io.pedestal.http/port (or (System/getenv "PORT") 3100)})

(def server-config
  {:service-map service-map
   :database database
   :auth auth-config})

(def migratus-config
  {:store         :database
   :migration-dir "migrations"
   :db            database})

;; (def config
;;   {:server/jetty {:handler (ig/ref :cheffy/app)
;;                   :port (or (System/getenv "PORT") 3000)}
;;    :cheffy/app {:jdbc-url (ig/ref :db/postgres)
;;                 :auth0 (ig/ref :auth/auth0)}
;;    :db/postgres {:jdbc-url db}
;;    :auth/auth0 {:auth0-client-secret "sd"}})