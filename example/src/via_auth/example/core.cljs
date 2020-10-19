(ns via-auth.example.core
  (:require [via-auth.example.app :refer [app]]
            [via-auth.example.events]
            [via-auth.example.subs]
            [via-auth.example.views :as views]
            [reagent.dom :as reagent]
            [re-frame.core :as re-frame]))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel] (.getElementById js/document "app")))

(defn ^:export init []
  (re-frame/dispatch-sync [:initialize-db])
  (enable-console-print!)
  (mount-root))
