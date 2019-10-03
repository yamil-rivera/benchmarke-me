This is a test for Sequential vs Concurrent approach on Java Rapidoid, Go net/http package and NodeJS

## Setup

### Java
- JDK8

### DB
```
$ sqlite3 sample.db
sqlite> CREATE TABLE tokens(id INTEGER PRIMARY KEY, encoded_token VARCHAR(255) NOT NULL);
```

## Benchmark
```
$ ab -n 50 -c 50  http://localhost:8080/stress/5

$ termgraph benchmark.txt

java/sequential            : ▇▇▇▇▇▇▇▇▇▇ 10.87
clojure-ring/sequential    : ▇▇▇▇▇▇▇▇▇▇▇▇▇▇▇▇▇▇▇▇▇▇▇▇▇▇▇▇▇▇ 30.95
clojure-pedestal/sequential: ▇▇▇▇▇▇▇▇▇▇▇▇▇▇▇▇▇▇▇▇▇▇▇▇▇▇▇▇▇▇ 30.54
```
