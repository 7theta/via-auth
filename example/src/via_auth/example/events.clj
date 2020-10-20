(ns via-auth.example.events
  (:require [via-auth.id-password :as id-password-auth]
            [via.events :refer [reg-event-via]]))

(defonce counter (atom 0))

(reg-event-via
 :api.example/id-password-auth-event
 [#'id-password-auth/interceptor]
 (fn [_ query-v]
   {:via/reply query-v
    :via/status 200}))
