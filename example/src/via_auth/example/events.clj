(ns via-auth.example.events
  (:require [via-auth.basic :as basic-auth]
            [via.events :refer [reg-event-via]]))

(defonce counter (atom 0))

(reg-event-via
 :api.example/basic-auth-event
 [#'basic-auth/interceptor]
 (fn [_ query-v]
   {:via/reply query-v
    :via/status 200}))
