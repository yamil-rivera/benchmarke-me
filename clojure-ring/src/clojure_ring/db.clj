(ns clojure-ring.db
  (:require [next.jdbc :as jdbc]
            [next.jdbc.connection :as connection])
  (:import (java.util Base64)
           (com.zaxxer.hikari HikariDataSource)))

(def db-spec {:dbtype "sqlite" :dbname "sample.db"})
(def conn (jdbc/get-connection db-spec))
;(def ^HikariDataSource conn-pool (connection/->pool HikariDataSource db-spec))

#_(jdbc/execute! db-spec ["
CREATE TABLE tokens (
  id INTEGER PRIMARY KEY,
  encoded_token VARCHAR(255) NOT NULL
)"])

(defn insert-token [^bytes token]
  (let [encoded-token (.encodeToString (Base64/getEncoder) token)]
    (jdbc/execute! conn ["INSERT INTO tokens(encoded_token) VALUES(?)" encoded-token])))
