(ns via-auth.example.config
  (:require [via.endpoint]
            [via.events]
            [via.subs]
            [via.fx]
            [via-auth.example.test-client]
            [integrant.core :as ig]
            [via.endpoint :as via]))

;;; Public

(def config
  {:via/endpoint
   {}

   :via/events
   {:endpoint (ig/ref :via/endpoint)}

   :via/subs
   {:endpoint (ig/ref :via/endpoint)}

   :via/fx
   {:endpoint (ig/ref :via/endpoint)}

   :via-auth.example/test-client
   {:endpoint (ig/ref :via/endpoint)}})
