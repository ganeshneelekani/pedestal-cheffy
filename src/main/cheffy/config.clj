(ns cheffy.config)

(def server-config
  {:service-map {:io.pedestal.http/type :jetty
                 :io.pedestal.http/join? false
                 :io.pedestal.http/port (or (System/getenv "PORT") 3100)}})

(def db {:dbname   (or (System/getenv "DB_DBNAME") "it_data")
         :user     (or (System/getenv "DB_USER") "myuser")
         :password (or (System/getenv "DB_PASSWORD") "mypassword")
         :host     (or (System/getenv "DB_HOST") "localhost")
         :port     (or (System/getenv "DB_PORT") 5432)
         :dbtype   "postgresql"})

(def migratus-config
  {:store         :database
   :migration-dir "migrations1"
   :db            db})

;; (def config
;;   {:server/jetty {:handler (ig/ref :cheffy/app)
;;                   :port (or (System/getenv "PORT") 3000)}
;;    :cheffy/app {:jdbc-url (ig/ref :db/postgres)
;;                 :auth0 (ig/ref :auth/auth0)}
;;    :db/postgres {:jdbc-url db}
;;    :auth/auth0 {:auth0-client-secret "sd"}})