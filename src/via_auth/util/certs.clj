(ns via-auth.util.certs
  (:import [io.undertow.server HttpServerExchange]))

(defn ring-request->certs
  [request]
  (when-let [^HttpServerExchange exchange (:server-exchange request)]
    (some-> exchange
            (.getConnection)
            (.getSslSessionInfo)
            (.getPeerCertificates))))
