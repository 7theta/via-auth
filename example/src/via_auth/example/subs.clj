(ns via-auth.example.subs
  (:require [via-auth.id-password :as id-password-auth]
            [via.endpoint :as via]
            [signum.atom :as s]
            [signum.subs :refer [reg-sub subscribe]]
            [utilis.fn :refer [fsafe]]))

(reg-sub
 :api.example/id-password-auth-sub
 [#'via/interceptor #'id-password-auth/interceptor]
 (fn [query-v] query-v))
