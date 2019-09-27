package main

import (
	"crypto/sha256"
	"database/sql"
	"encoding/base64"
	"log"
	"math/rand"
	"net/http"
	"strconv"
	"sync"

	_ "github.com/go-sql-driver/mysql"
)

const numberOfBytesInSha256Block int = 256 / 8
const numberOfHashesPerOperation int = 1000
const serverPort int = 8081

func cpuWorkOut(tickets <-chan int, process chan<- [numberOfBytesInSha256Block]byte) {
	// Calculate SHA256 of a random byte slice of 256 bits / 32 bytes in order to simulate CPU
	select {
	case <-tickets:
		token := make([]byte, numberOfBytesInSha256Block)
		_, err := rand.Read(token)
		if err != nil {
			log.Printf("Failed to generate random number with token %v", token)
			return
		}
		var fixedToken [numberOfBytesInSha256Block]byte
		for i := 0; i < numberOfHashesPerOperation; i++ {
			fixedToken = sha256.Sum256(token)
			copy(fixedToken[:], token)
		}
		process <- fixedToken
	default:
		close(process)
	}
}

var db *sql.DB
var ioMutex sync.Mutex

func ioWorkOut(process <-chan [numberOfBytesInSha256Block]byte, done chan<- int) {
	// Save token to MySQL DB to simulate I/O
	encryptedToken := <-process
	encodedToken := base64.StdEncoding.EncodeToString(encryptedToken[:])
	ioMutex.Lock()
	db.Exec("INSERT INTO tokens(encoded_token) VALUES ('" + encodedToken + "');")
	ioMutex.Unlock()
	done <- 1
}

func handler(w http.ResponseWriter, r *http.Request) {
	loopCountParam := r.URL.Path[len("/stress/"):]
	loopCount, err := strconv.Atoi(loopCountParam)
	if err != nil {
		log.Printf("Failed to parse integer from path %v", loopCountParam)
		return
	}
	tickets := make(chan int, loopCount)
	process := make(chan [numberOfBytesInSha256Block]byte)
	done := make(chan int)
	for i := 0; i < loopCount; i++ {
		// Issue tickets
		tickets <- i
	}
	for i := 0; i < loopCount; i++ {
		// Spawn workers
		go cpuWorkOut(tickets, process)
		go ioWorkOut(process, done)
	}
	ticketsFinished := 0
	for i := range done {
		// Count finished tickets
		ticketsFinished += i
		if ticketsFinished == loopCount {
			close(done)
		}
	}
	w.Write([]byte("I worked out " + strconv.Itoa(loopCount) + " times!"))
}

func main() {
	var err error
	db, err = sql.Open("mysql", "mysql:Welcome@/encrypted_tokens")
	if err != nil {
		log.Printf("Failed to create DB connection %v", err)
		return
	}
	defer db.Close()
	http.HandleFunc("/stress/", handler)
	log.Fatal(http.ListenAndServe(":"+strconv.Itoa(serverPort), nil))
}
