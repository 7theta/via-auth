(ns via-auth.example.test-client
  (:require [via.endpoint :refer [subscribe dispose] :as via]
            [re-frame.core :refer [dispatch]]
            [integrant.core :as ig]))

(defmethod ig/init-key :via-auth.example/test-client
  [_ {:keys [endpoint]}]
  {:endpoint endpoint
   :sub-key (subscribe endpoint
                       {:open #(js/console.log "WebSocket Connected" (pr-str %))
                        :close #(js/console.log "WebSocket Disconnected" (pr-str %))})})

(defmethod ig/halt-key! :via-auth.example/test-client
  [_ {:keys [endpoint sub-key]}]
  (dispose endpoint sub-key))
