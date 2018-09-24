from threading import Semaphore, Event, RLock, Barrier, BrokenBarrierError, Thread
import random, time, os, signal

# constants
THREADS = 10 # assumed to be greater than 0
TIMEOUT = 2 # seconds

class ThreadManager:

    """
    Create a new ThreadManager object.
    """
    def __init__(self):
        self.threads = []
        self.ready = Event()
        self.synchronized = Semaphore(0)

        self.lock = RLock()
        self.started = False
        self.barrier = None
        self.cancelled = False

    """
    Start a thread.

    Args:
        The function to use as the thread entry point
        The arguments for the thread
    """
    def start_thread(self, fn, *args):
        self.lock.acquire()
        try:
            if self.started:
                raise ValueError("'start()' has already been called.")
            elif self.cancelled:
                raise ValueError("'cancel()' has already been called.")
            
            thread = Thread(target=fn, args=args)
            thread.start()
            self.threads.append(thread)
        finally:
            self.lock.release()

    """
    Start a series of identical threads.

    Args:
        Number of threads to start
        The function to use as the thread entry point
        The arguments for the thread
    """
    def start_threads(self, num, fn, *args):
        self.lock.acquire()
        try:
            for _ in range(num):
                self.start_thread(fn, *args)
        finally:
            self.lock.release()

    """
    Start the ThreadManager.
    """
    def start(self):
        self.lock.acquire()
        try:
            if self.cancelled:
                raise ValueError("'cancel()' has already been called.")

            self.started = True
            notify_synchronized = lambda: self.synchronized.release()
            self.barrier = Barrier(len(self.threads), notify_synchronized)
            self.ready.set()
        finally:
            self.lock.release()

    """
    Cancels all threads. Use caution when making this call, as
    any threads that are owned by the ThreadManager must check
    'is_cancelled()' periodically to ensure quick cancellation.
    If threads have not yet started then 'synchronize()' will
    return immediately with a value of 'False' following this.
    """
    def cancel(self):
        self.lock.acquire()
        try:
            self.cancelled = True
            if self.barrier != None: # if barrier=None then has not been started yet
                self.barrier.abort() # signal threads that manager was cancelled
                self.ready.set() # release blocked threads
        finally:
            self.lock.release()

    """
    Check if the ThreadManager has been cancelled.
    
    Returns:
        True if cancelled, False otherwise.
    """
    def is_cancelled(self):
        self.lock.acquire()
        try:
            return self.cancelled
        finally:
            self.lock.release()

    """
    Wait for all threads to have started.
    
    Args:
        Timeout in seconds (optional).
    Returns:
        True if successful, False otherwise.
    """
    def wait(self, timeout=None):
        return self.synchronized.acquire(timeout=timeout)

    """
    Wait for all threads to shutdown.
    
    Args:
        Timeout in seconds (optional).
    Returns:
        True if successful, False otherwise.
    """
    def join(self, timeout=None):
        for thread in self.threads:
            thread.join(timeout=timeout)
            if thread.is_alive():
                return False
        return True

    """
    Wait for all other threads to start. Must only be called by
    threads that are conceptually owned by the ThreadManager.

    Args:
        Timeout in seconds (optional).
    Returns:
        True if successful, False otherwise.
    """
    def synchronize(self, timeout=None):
        if self.ready.wait(timeout=timeout) == False:
            return False # timeout expired waiting for manager start
        # manager object has been started/cancelled

        # wait for all threads to start, or cancellation
        try:
            self.barrier.wait()
        except BrokenBarrierError:
            return False # cancellation occurred

        return True


def main():

    manager = ThreadManager()

    for i in range(THREADS):
        r = random.randint(0, 20)
        if r == 0: # 1/20 threads are bad
            # poison the thread pool
            manager.start_thread(bad_worker)
        else:
            manager.start_thread(worker, manager)

    manager.start()

    if manager.wait(TIMEOUT) == False:
        print("Thread did not start within a reasonable timeframe.")
        manager.cancel()

    if manager.join(TIMEOUT) == False:
        print("Thread did not exit within a reasonable timeframe.")
        os.kill(os.getpid(), signal.SIGTERM) # kill rogue threads

def worker(manager):
    if manager.synchronize() == False:
        print("Exiting early due to failed state.")
        return

    print("Performing action.")

# represents a thread that never starts properly
def bad_worker():
    while True:
        time.sleep(10)


if __name__ == "__main__":
    main()
