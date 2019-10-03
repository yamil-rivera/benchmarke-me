(ns clojure-pedestal.service
  (:require [clojure-pedestal.db :as db]
            [io.pedestal.http :as http]
            [ring.util.response :as ring-resp])
  (:import (java.util Random)
           (java.security MessageDigest)))

(def NUMBER_OF_HASHES_PER_OPERATION 1000)
(def NUMBER_OF_BYTES_IN_SHA256_BLOCK 32)

(defn- cpu-workout []
  (let [token (byte-array NUMBER_OF_BYTES_IN_SHA256_BLOCK)]
    (-> (new Random) (.nextBytes token))
    (dotimes [_ NUMBER_OF_HASHES_PER_OPERATION]
      (.digest (MessageDigest/getInstance "SHA-256") token))
    token))

(defn- io-workout [^bytes token] (db/insert-token token))

(defn sequential-process
  [{{:keys [loop-count]} :path-params}]
  (dotimes [_ (Integer/parseInt loop-count)] (io-workout (cpu-workout)))
  (ring-resp/response (str "I worked out " loop-count " times!")))

;; Tabular routes
(def routes #{["/stress/:loop-count" :get `sequential-process]})

;; Consumed by clojure-pedestal.server/create-server
;; See http/default-interceptors for additional options you can configure
(def service {:env                     :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; ::http/interceptors []
              ::http/routes            routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::http/allowed-origins ["scheme://host:port"]

              ;; Tune the Secure Headers
              ;; and specifically the Content Security Policy appropriate to your service/application
              ;; For more information, see: https://content-security-policy.com/
              ;;   See also: https://github.com/pedestal/pedestal/issues/499
              ;;::http/secure-headers {:content-security-policy-settings {:object-src "'none'"
              ;;                                                          :script-src "'unsafe-inline' 'unsafe-eval' 'strict-dynamic' https: http:"
              ;;                                                          :frame-ancestors "'none'"}}

              ;; Root for resource interceptor that is available by default.
              ::http/resource-path     "/public"

              ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
              ;;  This can also be your own chain provider/server-fn -- http://pedestal.io/reference/architecture-overview#_chain_provider
              ::http/type              :jetty
              ;;::http/host "localhost"
              ::http/port              8080
              ;; Options to pass to the container (Jetty)
              ::http/container-options {:h2c? true
                                        :h2?  false
                                        ;:keystore "test/hp/keystore.jks"
                                        ;:key-password "password"
                                        ;:ssl-port 8443
                                        :ssl? false
                                        ;; Alternatively, You can specify you're own Jetty HTTPConfiguration
                                        ;; via the `:io.pedestal.http.jetty/http-configuration` container option.
                                        ;:io.pedestal.http.jetty/http-configuration (org.eclipse.jetty.server.HttpConfiguration.)
                                        }})
