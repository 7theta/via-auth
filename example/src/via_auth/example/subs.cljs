(ns via-auth.example.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 :via-auth.example/authenticated?
 (fn [db _]
   (get-in db [:authenticated :token])))

(reg-sub
 :via-auth.example/count
 (fn [db _]
   (get db :counter 0)))
