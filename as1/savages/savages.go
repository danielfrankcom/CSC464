package main

import (
    "fmt"
    "time"
    "sync"
)

type empty struct{}
type semaphore chan empty

func (s semaphore) acquire() {
    e := empty{}
    s<-e
}

func (s semaphore) release() {
    <-s
}

var CAPACITY = 20 // servings in a pot
var COOKS = 2
var COOK_QUOTA = 5000
var SAVAGES = COOKS * 4
var SAVAGE_QUOTA = (COOKS * COOK_QUOTA * CAPACITY) / SAVAGES

// primitives
var servings int = 0
var mutex = make(semaphore, 1)
var emptyPot = make(semaphore, 0)
var fullPot = make(semaphore, 0)

var exited = 0

func main() {
    startTime := time.Now()

    wg := new(sync.WaitGroup)
    wg.Add(COOKS + SAVAGES)

    for i := 0; i < COOKS; i++ {
        go cook(wg)
    }
    for i := 0; i < SAVAGES; i++ {
        go savage(wg)
    }

    wg.Wait()

    fmt.Printf("--- %s ---\n", time.Since(startTime))
}

func cook(wg *sync.WaitGroup) {
    defer wg.Done()

    for cooked := 0; cooked < COOK_QUOTA; cooked++ {
        emptyPot.acquire()
        fillPot()
        fullPot.release()
    }
}

func fillPot() {
    fmt.Println("Pot is now full")
}

func savage(wg *sync.WaitGroup) {
    defer wg.Done()

    for eaten := 0; eaten < SAVAGE_QUOTA; eaten++ {
        mutex.acquire()

        if (servings == 0) {
            emptyPot.release()
            fullPot.acquire()
            servings = CAPACITY
        }

        servings --
        getServing()

        mutex.release()

        fmt.Println("Savage is eating")
    }
}

func getServing() {
    fmt.Println("Savage retreived serving")
}
