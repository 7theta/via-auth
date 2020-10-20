(ns via-auth.example.views
  (:require [via.subs :refer [subscribe]]
            [re-frame.core :refer [dispatch]]))

(defn main-panel []
  [:div {:style {:margin "40px"}}
   [:span (str "Connected: " @(subscribe [:via.endpoint/connected]))]
   [:br]
   (if @(subscribe [:via-auth.example/authenticated?])
     [:div
      [:button {:on-click #(dispatch [:via-auth.example/logout])} [:font {:size "+1"} "Logout"]]
      [:br]
      [:div {:style {:margin-top "20px"}}
       "ID/Password Authenticated Sub: " (str @(subscribe [:api.example/id-password-auth-sub ::authenticated]))]]
     [:button {:on-click #(dispatch [:via-auth.example/login])} [:font {:size "+1"} "Login"]])])
