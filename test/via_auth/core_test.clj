(ns via-auth.core-test
  (:require [via-auth.id-password :as id-password]

            [via.util.id :refer [uuid]]
            [via.endpoint :as via]
            [via.subs :as vs]
            [via.events :as ve]
            [via.defaults :refer [default-via-endpoint]]
            [via.core :as vc]

            [clojure.data :refer [diff]]

            [signum.subs :as ss]
            [signum.signal :as sig]
            [signum.events :as se]
            [signum.fx :as sfx]

            [integrant.core :as ig]

            [compojure.core :as compojure :refer [GET POST]]
            [compojure.route :as route]
            [ring.middleware.defaults :as ring-defaults]

            [utilis.timer :as timer]

            [clojure.test :as t]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop :include-macros true]
            [clojure.test.check.clojure-test :refer [defspec]]
            [via.adapter :as adapter])
  (:import [java.net ServerSocket]))

;;; Endpoint Setup

(def lock (Object.))

(defn- allocate-free-port!
  []
  (locking lock
    (let [socket (ServerSocket. 0)]
      (.setReuseAddress socket true)
      (let [port (.getLocalPort socket)]
        (try (.close socket) (catch Exception _))
        port))))

(defmethod ig/init-key :via.core-test/ring-handler
  [_ {:keys [via-handler]}]
  (-> (compojure/routes
       (GET default-via-endpoint ring-req (via-handler ring-req)))
      (ring-defaults/wrap-defaults ring-defaults/site-defaults)))

(defn default-event-listener
  [[event-id event]]
  (when (not (#{:via.endpoint.peer/connected
                :via.endpoint.peer/disconnected
                :via.endpoint.peer/removed} event-id))
    (locking lock
      (println event-id event))))

(defn peer
  ([] (peer nil))
  ([{:keys [port integrant-config] :as endpoint-config}]
   (let [endpoint-config (dissoc endpoint-config :port :integrant-config)]
     (loop [attempts 3]
       (let [result (try (let [port (or port (allocate-free-port!))
                               peer (ig/init
                                     (merge {:via/endpoint endpoint-config
                                             :via/subs {:endpoint (ig/ref :via/endpoint)}
                                             :via/http-server {:ring-handler (ig/ref :via.core-test/ring-handler)
                                                               :http-port port}
                                             :via.core-test/ring-handler {:via-handler (ig/ref :via/endpoint)}}
                                            integrant-config))]
                           {:peer peer
                            :port port
                            :endpoint (:via/endpoint peer)
                            :shutdown #(ig/halt! peer)
                            :address (str "ws://localhost:" port default-via-endpoint)})
                         (catch Exception e
                           (if (zero? attempts)
                             (throw e)
                             ::recur)))]
         (if (not= result ::recur)
           result
           (recur (dec attempts))))))))

(defn shutdown
  [{:keys [shutdown] :as peer}]
  (shutdown))

(defn connect
  [from to]
  (via/connect (:endpoint from) (:address to)))

(defn wait-for
  ([p] (wait-for p 5000))
  ([p timeout-ms]
   (let [result (deref p timeout-ms ::timed-out)]
     (if (= result ::timed-out)
       (throw (ex-info "Timed out waiting for promise" {}))
       result))))

;;; id-password

(defspec id-password-authenticate
  30
  (prop/for-all [password gen/string-alphanumeric
                 authenticate? gen/boolean]
                (let [user-id (uuid)
                      event-id (str (gensym) "/event")
                      peer-1 (peer {:exports {:events #{event-id}}
                                    :integrant-config {:via-auth/id-password {:endpoint (ig/ref :via/endpoint)
                                                                              :query-fn (fn [id]
                                                                                          (when (= id user-id)
                                                                                            {:id id
                                                                                             :password (id-password/hash-password password)}))}}})
                      peer-2 (peer)]
                  (try (let [reply (try @(vc/dispatch
                                          (:endpoint peer-2)
                                          (connect peer-2 peer-1)
                                          [:via.auth/id-password-login {:id user-id
                                                                        :password (if authenticate?
                                                                                    password
                                                                                    (str password "_" (gensym)))}])
                                        (catch Exception e
                                          (:error (ex-data e))))]
                         (if authenticate?
                           (= 200 (:status reply))
                           (= 403 (:status reply))))
                       (catch Exception e
                         (locking lock
                           (println e))
                         false)
                       (finally
                         (shutdown peer-1)
                         (shutdown peer-2))))))

(defspec id-password-authorize
  30
  (prop/for-all [password gen/string-alphanumeric
                 authorize? gen/boolean]
                (let [user-id (uuid)
                      event-id (str (gensym) "/event")
                      peer-1 (peer {:exports {:events #{event-id}}
                                    :integrant-config {:via-auth/id-password {:endpoint (ig/ref :via/endpoint)
                                                                              :query-fn (fn [id]
                                                                                          (when (= id user-id)
                                                                                            {:id id
                                                                                             :password (id-password/hash-password password)}))}}})
                      peer-2 (peer)
                      peer-1->2-id (connect peer-2 peer-1)]
                  (se/reg-event
                   event-id
                   [(-> peer-1 :peer :via-auth/id-password :interceptor)]
                   (fn [_ _]
                     {:via/reply {:status 200}}))
                  (try (try @(vc/dispatch
                              (:endpoint peer-2)
                              peer-1->2-id
                              [:via.auth/id-password-login {:id user-id
                                                            :password (if authorize?
                                                                        password
                                                                        (str password "_" (gensym)))}])
                            (catch Exception _))
                       (let [reply (try @(vc/dispatch (:endpoint peer-2) peer-1->2-id [event-id])
                                        (catch Exception e
                                          (:error (ex-data e))))]
                         (if authorize?
                           (= 200 (:status reply))
                           (= 403 (:status reply))))
                       (catch Exception e
                         (locking lock
                           (println e))
                         false)
                       (finally
                         (shutdown peer-1)
                         (shutdown peer-2))))))

(defspec id-password-logout
  30
  (prop/for-all [password gen/string-alphanumeric]
                (let [user-id (uuid)
                      event-id (str (gensym) "/event")
                      peer-1 (peer {:exports {:events #{event-id}}
                                    :integrant-config {:via-auth/id-password {:endpoint (ig/ref :via/endpoint)
                                                                              :query-fn (fn [id]
                                                                                          (when (= id user-id)
                                                                                            {:id id
                                                                                             :password (id-password/hash-password password)}))}}})
                      peer-2 (peer)
                      peer-1->2-id (connect peer-2 peer-1)]
                  (se/reg-event
                   event-id
                   [(-> peer-1 :peer :via-auth/id-password :interceptor)]
                   (fn [_ _]
                     {:via/reply {:status 200}}))
                  (try @(vc/dispatch (:endpoint peer-2) peer-1->2-id
                                     [:via.auth/id-password-login {:id user-id
                                                                   :password password}])
                       @(vc/dispatch (:endpoint peer-2) peer-1->2-id [:via.auth/logout])
                       (let [reply (try @(vc/dispatch (:endpoint peer-2) peer-1->2-id [event-id])
                                        (catch Exception e
                                          (:error (ex-data e))))]
                         (= 403 (:status reply)))
                       (catch Exception e
                         (locking lock
                           (println e))
                         false)
                       (finally
                         (shutdown peer-1)
                         (shutdown peer-2))))))
