package main

import (
    "fmt"
    "time"
    "sync"
    "math/rand"
)


type Null struct{}

type Person struct {
    gender string
}


// constants
const EACH_GENDER = 1000000
const TOTAL = 2 * EACH_GENDER

// channels
var queue = make(chan Person)
var bathroom = make(chan Person, 3)
var switchGenders = make(chan Null, 0)


func main() {
    startTime := time.Now()

    rand.Seed(time.Now().UTC().UnixNano())

    wg := new(sync.WaitGroup)
    wg.Add(2)

    go manager(wg)
    go attendant(wg)

    currentMen := 0
    currentWomen := 0
    for i := 0; i < TOTAL; i++ {
        random := rand.Intn(2)
        if ((random == 0 && currentMen != EACH_GENDER) || currentWomen == EACH_GENDER) {
            queue <- Person {"Man"}
            currentMen ++
        } else {
            queue <- Person {"Woman"}
            currentWomen ++
        }
    }

    wg.Wait()

    fmt.Printf("--- %s ---\n", time.Since(startTime))
}

func manager(wg *sync.WaitGroup) {
    defer wg.Done()

    lastGender := "none"
    for i := 0; i < TOTAL; i++ {
        person := <-queue // Pull someone from the queue.

        if (person.gender != lastGender) {
            switchGenders <- Null{} // Wait for all people of other gender to leave the bathroom.
        }
        lastGender = person.gender

        bathroom <- person // Put the person in the bathroom. Block if the bathroom is full.
        fmt.Printf("%s using bathroom.\n", person.gender)
    }
}

func attendant(wg *sync.WaitGroup) {
    defer wg.Done()

    <-switchGenders // Unblock the manager goroutine
    for i := 0; i < TOTAL; i++ {
        success := false
        for !success {
            select {
                case <-bathroom: // Pull someone from the bathroom (they are done).
                    success = true
                case <-switchGenders: // Notify that the bathroom is empty.
                default:
            }
        }
    }
}
