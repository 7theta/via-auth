(ns via-auth.example.ring-handler
  (:require [via-auth.util.certs :as certs]
            [via.defaults :refer [default-via-endpoint]]
            [compojure.core :as compojure :refer [GET POST]]
            [compojure.route :as route]
            [ring.util.response :as response]
            [ring.middleware.defaults :as ring-defaults]
            [integrant.core :as ig]))

(defn wrap-peer-certs
  [handler]
  (fn [request]
    (handler
     (if-let [certs (certs/ring-request->certs request)]
       (assoc request :peer-certs certs)
       request))))

(defmethod ig/init-key :via-auth.example/ring-handler [_ {:keys [via-handler]}]
  (-> (compojure/routes
       (GET "/" req-req (response/content-type
                         (response/resource-response "public/index.html")
                         "text/html"))
       (GET default-via-endpoint ring-req (via-handler ring-req))
       (route/resources "/"))
      (wrap-peer-certs)
      (ring-defaults/wrap-defaults ring-defaults/site-defaults)))
