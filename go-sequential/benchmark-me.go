package main

import (
	"crypto/sha256"
	"database/sql"
	"encoding/base64"
	"log"
	"math/rand"
	"net/http"
	"strconv"

	_ "github.com/go-sql-driver/mysql"
)

const numberOfBytesInSha256Block int = 256 / 8
const numberOfHashesPerOperation int = 1000
const serverPort int = 8081

func cpuWorkOut() []byte {
	// Calculate SHA256 of a random byte slice of 256 bits / 32 bytes in order to simulate CPU
	token := make([]byte, numberOfBytesInSha256Block)
	_, err := rand.Read(token)
	if err != nil {
		log.Printf("Failed to generate random number with token %v", token)
		return nil
	}
	var fixedToken [numberOfBytesInSha256Block]byte
	for i := 0; i < numberOfHashesPerOperation; i++ {
		fixedToken = sha256.Sum256(token)
		copy(fixedToken[:], token)
	}
	return token
}

var db *sql.DB

func ioWorkOut(encryptedToken []byte) {
	// Save token to MySQL DB to simulate I/O
	encodedToken := base64.StdEncoding.EncodeToString(encryptedToken[:])
	db.Exec("INSERT INTO tokens(encoded_token) VALUES ('" + encodedToken + "');")
}

func handler(w http.ResponseWriter, r *http.Request) {
	loopCountParam := r.URL.Path[len("/stress/"):]
	loopCount, err := strconv.Atoi(loopCountParam)
	if err != nil {
		log.Printf("Failed to parse integer from path %v", loopCountParam)
		return
	}
	for i := 0; i < loopCount; i++ {
		ioWorkOut(cpuWorkOut())
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
