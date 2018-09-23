package main

import (
    "fmt"
    "time"
    "sync"
    "math/rand"
)


type Oxygen struct {}
type Hydrogen struct {}

type Water struct {
    index int
    oxygen Oxygen
    hydrogen1 Hydrogen
    hyrdogen2 Hydrogen
}

// constants
const OXYGEN = 10000
const HYDROGEN = 2 * OXYGEN
const TOTAL = OXYGEN + HYDROGEN

// channels
var oxyQueue = make(chan Oxygen, OXYGEN)
var hydQueue = make(chan Hydrogen, HYDROGEN)

func main() {
    startTime := time.Now()

    rand.Seed(time.Now().UTC().UnixNano())

    wg := new(sync.WaitGroup)
    wg.Add(1)

    go bonder(wg)

    currentOxy := 0
    currentHyd := 0
    for i := 0; i < TOTAL; i++ {
        random := rand.Intn(2)
        if ((random == 0 && currentOxy != OXYGEN) || currentHyd == HYDROGEN) {
            oxyQueue <- Oxygen {}
            currentOxy ++
        } else {
            hydQueue <- Hydrogen {}
            currentHyd ++
        }
    }

    wg.Wait()

    fmt.Printf("--- %s ---\n", time.Since(startTime))
}

func bonder(wg *sync.WaitGroup) {
    defer wg.Done()

    MOLECULES := TOTAL / 3
    for i := 1; i < MOLECULES + 1; i += 1 {
        oxygen := <-oxyQueue
        hydrogen1 := <-hydQueue
        hydrogen2 := <-hydQueue

        water := Water {
            i,
            oxygen,
            hydrogen1,
            hydrogen2,
        }

        fmt.Printf("%d bonded.\n", water.index)
    }
}
