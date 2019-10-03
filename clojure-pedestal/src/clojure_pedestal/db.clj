(ns clojure-pedestal.db
  (:require [next.jdbc :as jdbc])
  (:import (java.util Base64)))

(def db {:dbtype "sqlite" :dbname "sample.db"})
(def ds (jdbc/get-datasource db))

#_(jdbc/execute! ds ["
create table tokens (
  id int primary key,
  encoded_token varchar(255) not null
)"])

(defn insert-token [^bytes token]
  (let [encoded-token (.encodeToString (Base64/getEncoder) token)]
    (jdbc/execute! ds ["INSERT INTO tokens(encoded_token) VALUES(?)" encoded-token])))
