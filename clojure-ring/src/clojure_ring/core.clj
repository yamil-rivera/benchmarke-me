(ns clojure-ring.core
  (:gen-class)
  (:require [clojure.core.async :refer [chan to-chan pipeline <!!]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.coercions :refer [as-int]]
            [clojure-ring.db :as db]
            [ring.adapter.jetty :refer [run-jetty]])
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

(defn- sequential-process [loop-count]
  (dotimes [_ loop-count] (io-workout (cpu-workout)))
  (str "I worked out " loop-count " times!"))

(defn- async-process [loop-count]
  (let [req-ch (to-chan (range loop-count))
        token-ch (chan 10)
        done-ch (chan 10)
        cpus (.availableProcessors (Runtime/getRuntime))]
    (pipeline cpus token-ch (map (fn [_] (cpu-workout))) req-ch)
    (pipeline cpus done-ch (map io-workout) token-ch)
    ;; wait until done
    (while (not (nil? (<!! done-ch))) (print ".")))
  (str "I worked out " loop-count " times!"))

(defroutes app-routes
  (GET "/seq/:loop-count" [loop-count :<< as-int] (sequential-process loop-count))
  (GET "/async/:loop-count" [loop-count :<< as-int] (async-process loop-count))
  (route/not-found "Page not found"))

(defn -main []
  (-> app-routes
      (run-jetty {:port 8080})))
