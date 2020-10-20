(ns via-auth.example.app
  (:require [via-auth.example.config :refer [config]]
            [integrant.core :as ig]))

(defonce app (ig/init config))
