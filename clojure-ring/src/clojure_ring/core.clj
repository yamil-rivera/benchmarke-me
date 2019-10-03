(ns clojure-ring.core
  (:gen-class)
  (:require [compojure.core :refer :all]
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

(defroutes app-routes
  (GET "/stress/:loop-count" [loop-count :<< as-int]
    (do
      (dotimes [_ loop-count] (io-workout (cpu-workout)))
      (str "I worked out " loop-count " times!")))
  (route/not-found "Page not found"))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (-> app-routes
      (run-jetty {:port 8080})))
