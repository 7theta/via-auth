(ns via-auth.example.events
  (:require [via-auth.example.db :as db]
            [via.events :refer [reg-event-via]]
            [via.fx :as via-fx]
            [re-frame.core :refer [reg-event-db reg-event-fx]]
            [via.endpoint :as via]))

(reg-event-db
 :initialize-db
 (fn [_ _]
   db/default-db))

(reg-event-fx
 :via-auth.example/login
 (fn [{:keys [db]} _]
   {:via/dispatch
    {:event [:via/id-password-login {:id "admin" :password "admin"}]
     :on-success [:via-auth.example.login/succeeded]
     :on-failure [:via-auth.example.login/failed]
     :on-timeout [:via-auth.example.login/timed-out]}}))

(reg-event-fx
 :via-auth.example.login/succeeded
 (fn [{:keys [db]} [_ login-creds]]
   {:db (assoc db :authenticated login-creds)}))

(reg-event-db
 :via-auth.example.login/failed
 (fn [db error]
   (js/console.error ":via-auth.example.login/failed" (pr-str error))
   (dissoc db :authenticated)))

(reg-event-db
 :via-auth.example.login/timed-out
 (fn [db error]
   (js/console.error ":via-auth.example.login/timed-out" (pr-str error))
   (dissoc db :authenticated)))

(reg-event-fx
 :via-auth.example/logout
 (fn [{:keys [db]} _]
   {:db (dissoc db :authenticated)
    :via/dispatch {:event [:via/logout]}}))

(reg-event-fx
 :via-auth.example/increment-count
 (fn [_ _]
   {:via/dispatch {:event [:api.example/increment-count]
                   :on-success [:via-auth.example.increment-count/succeeded]
                   :on-failure [:via-auth.example.increment-count/failed]
                   :on-timeout [:via-auth.example.increment-count/timed-out]}}))

(reg-event-fx
 :via-auth.example.increment-count/succeeded
 (fn [{:keys [db]} [_ count]]
   (js/console.log ":via-auth.example.increment-count/succeeded" count)
   {:db (assoc db :counter count)}))

(reg-event-fx
 :via-auth.example.increment-count/failed
 (fn [{:keys [db]} [_ error]]
   (js/console.log ":via-auth.example.increment-count/failed" error)))

(reg-event-fx
 :via-auth.example.increment-count/timed-out
 (fn [_ error]
   (js/console.error ":via-auth.example.increment-count/timed-out" (pr-str error))))

(reg-event-via
 :via-auth.example/server-broadcast
 (fn [_ [_ message]]
   (js/console.log "Server Push:" (clj->js message))))
