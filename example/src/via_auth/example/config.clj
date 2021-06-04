(ns via-auth.example.config
  (:require [via-auth.example.events]
            [via-auth.example.subs]
            [integrant.core :as ig]))

(def config
  {:via/endpoint {}
   :via/events {:endpoint (ig/ref :via/endpoint)}
   :via/subs {:endpoint (ig/ref :via/endpoint)}
   :via/http-server {:ring-handler (ig/ref :via-auth.example/ring-handler)}

   :via-auth.example/user-store nil
   :via-auth.example/ring-handler {:via-handler (ig/ref :via/endpoint)}

   :via-auth/id-password {:endpoint (ig/ref :via/endpoint)
                          :query-fn (ig/ref [:via-auth.example/user-store])}})
