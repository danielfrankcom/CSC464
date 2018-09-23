from threading import Semaphore
from threading import Thread
import random
import time

class Barrier:
    def __init__(self, n):
        self.n = n
        self.count = 0
        self.mutex = Semaphore(1)
        self.turnstile = Semaphore(0)
        self.turnstile2 = Semaphore(0)
    def phase1(self):
        self.mutex.acquire()
        self.count += 1
        if self.count == self.n:
            for i in range(self.n):
                self.turnstile.release()
        self.mutex.release()
        self.turnstile.acquire()
    def phase2(self):
        self.mutex.acquire()
        self.count -= 1
        if self.count == 0:
            for i in range(self.n):
                self.turnstile2.release()
        self.mutex.release()
        self.turnstile2.acquire()
    def wait(self):
        self.phase1()
        self.phase2()

class Bonder:
    def __init__(self):
        self.bonded = 0
        self.currentOxy = 0
        self.currentHyd = 0

    def bond(self, identifier):
        if identifier == "oxygen":
            self.currentOxy += 1
        elif identifier == "hydrogen":
            self.currentHyd += 1
        else:
            raise ValueError("Invalid identifier")

        if self.currentOxy > 1 or self.currentHyd > 2:
            raise Exception("Invalid bond")
        elif self.currentOxy == 1 and self.currentHyd == 2:
            self.currentOxy = 0
            self.currentHyd = 0
            self.bonded += 1
            print("{:d} bonded.".format(self.bonded))


OXYGEN = 10_000
HYDROGEN = 2 * OXYGEN

# primitives
mutex = Semaphore(1)
oxygen = 0
hydrogen = 0
barrier = Barrier(3)
oxyQueue = Semaphore(0)
hydQueue = Semaphore(0)

bonder = Bonder()

def main():

    threads = []

    for i in range(OXYGEN):
        oxy_t = Thread(target=oxy, args=())
        threads.append(oxy_t)

    for i in range(HYDROGEN):
        hyd_t = Thread(target=hyd, args=())
        threads.append(hyd_t)

    random.shuffle(threads)

    for thread in threads:
        thread.start()

    for thread in threads:
        thread.join()

def oxy():
    global oxygen
    global hydrogen

    mutex.acquire()
    oxygen += 1
    if hydrogen >= 2:
        hydQueue.release()
        hydQueue.release()
        hydrogen -= 2
        oxyQueue.release()
        oxygen -= 1
    else:
        mutex.release()

    oxyQueue.acquire()
    bonder.bond("oxygen")

    barrier.wait()
    mutex.release()

def hyd():
    global oxygen
    global hydrogen

    mutex.acquire()
    hydrogen += 1
    if hydrogen >= 2 and oxygen >= 1:
        hydQueue.release()
        hydQueue.release()
        hydrogen -= 2
        oxyQueue.release()
        oxygen -= 1
    else:
        mutex.release()

    hydQueue.acquire()
    bonder.bond("hydrogen")

    barrier.wait()


if __name__ == "__main__":
    start_time = time.time()
    main()
    print("--- %s seconds ---" % (time.time() - start_time))
