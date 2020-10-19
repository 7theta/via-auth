(ns via-auth.example.subs
  (:require [via-auth.basic :as basic-auth]
            [via.endpoint :as via]
            [signum.atom :as s]
            [signum.subs :refer [reg-sub subscribe]]
            [utilis.fn :refer [fsafe]]))

(reg-sub
 :api.example/basic-auth-sub
 [#'via/interceptor #'basic-auth/interceptor]
 (fn [query-v] query-v))
