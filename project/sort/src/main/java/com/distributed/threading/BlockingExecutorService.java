package com.distributed.threading;

import java.util.List;
import java.util.concurrent.*;

public class BlockingExecutorService extends AbstractExecutorService {

    /**
     * Represents a {@link Runnable} task that releases the {@link Semaphore}
     * when it has finished running so that other tasks may continue.
     */
    private static class ExecutorTask implements Runnable {

        /**
         * Create a new {@link ExecutorTask} object.
         *
         * @param runnable The {@link Runnable} that contains the task for
         *                 this object to execute.
         * @param parent   The {@link BlockingExecutorService} that owns
         *                 this object.
         */
        ExecutorTask(Runnable runnable, BlockingExecutorService parent) {
            mRunnable = runnable;
            mParent = parent;
        }

        private final Runnable mRunnable;
        private final BlockingExecutorService mParent;

        @Override
        public void run() {
            try {
                mRunnable.run();
            } finally {
                mParent.mFreeThreads.release();
            }
        }

    }

    /**
     * Create a new {@link BlockingExecutorService} object.
     *
     * @param numThreads The number of threads to create in the thread pool.
     */
    public BlockingExecutorService(int numThreads) {
        mExecutorService = Executors.newFixedThreadPool(numThreads);
        mFreeThreads = new Semaphore(numThreads, true);
    }

    private final ExecutorService mExecutorService;
    private final Semaphore mFreeThreads;

    @Override
    public void shutdown() {
        mExecutorService.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return mExecutorService.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return mExecutorService.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return mExecutorService.isTerminated();
    }

    @Override
    public boolean awaitTermination(long l, TimeUnit timeUnit) throws InterruptedException {
        return mExecutorService.awaitTermination(l, timeUnit);
    }

    @Override
    public void execute(Runnable runnable) {
        // Block this method from continuing until the executor is available.
        mFreeThreads.acquireUninterruptibly();
        final Runnable task = new ExecutorTask(runnable, this);
        mExecutorService.execute(task);
    }

}
