from threading import Semaphore, Barrier, BrokenBarrierError, Thread
import random, time, os, signal

# constants
THREADS = 10 # assumed to be greater than 0
TIMEOUT = 2 # seconds


def main():

    threads = []

    """
    Threads wait on the 'start' barrier until all threads are ready. When they are,
    one thread signals the 'ready' semaphore by using the 'notify' lambda.
    """
    ready = Semaphore(0)
    notify = lambda: ready.release()
    start = Barrier(THREADS, notify)

    for i in range(THREADS):
        thread = None

        r = random.randint(0, 20)
        if r == 0: # 1/20 threads are bad
            # poison the thread pool
            thread = Thread(target=bad_worker, args=())
        else:
            thread = Thread(target=worker, args=(start, ))
        thread.start()
        threads.append(thread)

    error_occurred = False

    if ready.acquire(timeout=TIMEOUT) == False: # wait for threads to be ready
        # call above expired and not all threads are ready
        print("Thread did not start within a reasonable timeframe.")
        error_occurred = True
        start.abort()

    for thread in threads:
        thread.join(timeout=TIMEOUT) # wait for threads to return
        if thread.is_alive():
            # call above expired and the thread is still running
            print("Thread did not exit within a reasonable timeframe.")
            error_occurred = True
            break

    if error_occurred:
        os.kill(os.getpid(), signal.SIGTERM) # kill rogue threads

def worker(start):
    try:
        start.wait() # wait for all threads to start
    except BrokenBarrierError:
        # a thread didn't start properly, so exit early
        print("Exiting early due to failed state.")
        return

    print("Performing action.")

# represents a thread that never starts properly
def bad_worker():
    while True:
        time.sleep(10)


if __name__ == "__main__":
    main()
