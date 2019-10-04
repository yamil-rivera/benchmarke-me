(ns clojure-ring.db
  (:require [next.jdbc :as jdbc]
            [next.jdbc.connection :as connection])
  (:import (java.util Base64)
           (com.zaxxer.hikari HikariDataSource)))

(def db {:dbtype "sqlite" :dbname "sample.db"})
;(def conn (jdbc/get-connection db-spec))
(def ^HikariDataSource ds (connection/->pool HikariDataSource db))

#_(jdbc/execute! ds ["
CREATE TABLE tokens (
  id INTEGER PRIMARY KEY,
  encoded_token VARCHAR(255) NOT NULL
)"])

(defn insert-token [^bytes token]
  (let [encoded-token (.encodeToString (Base64/getEncoder) token)]
    (jdbc/execute! ds ["INSERT INTO tokens(encoded_token) VALUES(?)" encoded-token])))
