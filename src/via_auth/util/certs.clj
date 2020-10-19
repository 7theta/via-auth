(ns via-auth.util.certs
  (:import [io.undertow.server HttpServerExchange]))

(defn ring-request->certs
  [request]
  (when-let [^HttpServerExchange server-exchange (:server-exchange request)]
    (when-let [ssl (-> server-exchange (.getConnection) (.getSslSessionInfo))]
      (.getPeerCertificates ssl))))
