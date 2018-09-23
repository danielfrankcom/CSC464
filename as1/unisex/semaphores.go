package main

import (
    "fmt"
    "time"
    "sync"
    "math/rand"
)


type Null struct{}
type Semaphore chan Null

func (sem Semaphore) acquire() {
    n := Null{}
    sem<-n
}

func (sem Semaphore) release() {
    <-sem
}

type LightSwitch struct {
    counter int
    mutex Semaphore
}

func (sw *LightSwitch) lock(sem Semaphore) {
    sw.mutex.acquire()
    defer sw.mutex.release()

    sw.counter += 1
    if (sw.counter == 1) {
        sem.acquire()
    }
}

func (sw *LightSwitch) unlock(sem Semaphore) {
    sw.mutex.acquire()
    defer sw.mutex.release()

    sw.counter -= 1
    if (sw.counter == 0) {
        sem.release()
    }
}


// constants
const EACH_GENDER = 1000000
const TOTAL = 2 * EACH_GENDER

// primitives
var empty = make(Semaphore, 1)
var turnstile = make(Semaphore, 1)
var maleMultiplex = make(Semaphore, 3)
var femaleMultiplex = make(Semaphore, 3)
var maleSwitch = LightSwitch{ 0, make(Semaphore, 1) }
var femaleSwitch = LightSwitch{ 0, make(Semaphore, 1) }


func main() {
    startTime := time.Now()

    rand.Seed(time.Now().UTC().UnixNano())

    wg := new(sync.WaitGroup)
    wg.Add(TOTAL)

    currentMen := 0
    currentWomen := 0
    for i := 0; i < TOTAL; i++ {
        random := rand.Intn(2)
        if ((random == 0 && currentMen != EACH_GENDER) || currentWomen == EACH_GENDER) {
            go man(wg)
            currentMen ++
        } else {
            go woman(wg)
            currentWomen ++
        }
    }

    wg.Wait()

    fmt.Printf("--- %s ---\n", time.Since(startTime))
}

func man(wg *sync.WaitGroup) {
    defer wg.Done()

    turnstile.acquire()
    maleSwitch.lock(empty)
    defer maleSwitch.unlock(empty)
    turnstile.release()

    maleMultiplex.acquire()
    fmt.Println("Man using bathroom.")
    maleMultiplex.release()
}

func woman(wg *sync.WaitGroup) {
    defer wg.Done()

    turnstile.acquire()
    femaleSwitch.lock(empty)
    defer femaleSwitch.unlock(empty)
    turnstile.release()

    femaleMultiplex.acquire()
    fmt.Println("Woman using bathroom.")
    femaleMultiplex.release()
}
