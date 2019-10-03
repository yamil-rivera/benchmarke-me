(ns clojure-pedestal.db
  (:require [next.jdbc :as jdbc])
  (:import (java.util Base64)))

(def db-spec {:dbtype "sqlite" :dbname "sample.db"})
(def conn (jdbc/get-connection db-spec))

#_(jdbc/execute! db-spec ["
CREATE TABLE tokens (
  id INTEGER PRIMARY KEY,
  encoded_token VARCHAR(255) NOT NULL
)"])

(defn insert-token [^bytes token]
  (let [encoded-token (.encodeToString (Base64/getEncoder) token)]
    (jdbc/execute! conn ["INSERT INTO tokens(encoded_token) VALUES(?)" encoded-token])))
