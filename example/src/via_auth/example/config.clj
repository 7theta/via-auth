(ns via-auth.example.config
  (:require [via-auth.example.events]
            [via-auth.example.subs]
            [integrant.core :as ig]))

;; before enabling https and/or mtls, run gencerts.sh in the resources directory
(def enable-https? true)
(def enable-mtls? true)

(def config
  {:via/endpoint {}
   :via/events {:endpoint (ig/ref :via/endpoint)}
   :via/subs {:endpoint (ig/ref :via/endpoint)}
   :via/http-server (merge {:ring-handler (ig/ref :via-auth.example/ring-handler)}
                           (when enable-https?
                             {:https-port 3450
                              :keystore (clojure.java.io/resource "server/identity.jks")
                              :key-password "secret"})
                           (when enable-mtls?
                             {:truststore (clojure.java.io/resource "server/truststore.jks")
                              :trust-password "secret"
                              :client-auth :required}))

   :via-auth.example/user-store nil
   :via-auth.example/ring-handler {:via-handler (ig/ref :via/endpoint)}

   :via-auth/basic
   {:endpoint (ig/ref :via/endpoint)
    :query-fn (ig/ref [:via-auth.example/user-store])}})
