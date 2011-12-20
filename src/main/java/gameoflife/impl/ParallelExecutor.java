package gameoflife.impl;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CyclicBarrier;

/**
 * Instances of this class are expected to be used by exactly
 * one thread like this:<pre>
 *     ParallelExecutor parallelExecutor = new ParallelExecutor();
 *     ...
 *     parallelExecutor.addTask(...);
 *     ...
 *     parallelExecutor.addTask(...);
 *     parallelExecutor.finishAllAddedTasks();     
 * </pre>
 * Calls to {@link #addTask} add the given task to a queue.
 * When {@link #finishAllAddedTasks()} is called, all added
 * tasks are executed utilizing as many CPU cores as are
 * available.
 */
public class ParallelExecutor {
    
    private class BackgroundThread extends Thread {
        private BackgroundThread(int i) {
            super("Background thread #" + i + " of " + ParallelExecutor.this);
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                for (;;) {
                    _startBarrier.await();
                    Runnable task;
                    while ((task = _tasks.poll()) != null) {
                        task.run();
                    }
                    _finishBarrier.await();
                }
            } catch (Throwable t) {
                // TODO: exception handling
            }
        }
    }

    private final Queue<Runnable> _tasks;
    private final CyclicBarrier _startBarrier;
    private final CyclicBarrier _finishBarrier;
    private final BackgroundThread[] _backgroundThreads;

    public ParallelExecutor() {
        int n = Runtime.getRuntime().availableProcessors() - 1;
        if (n == 0) {
            throw new RuntimeException("Only 1 CPU core available.");
        }
        _tasks = new ConcurrentLinkedQueue<Runnable>();
        _startBarrier = new CyclicBarrier(n + 1);
        _finishBarrier = new CyclicBarrier(n + 1);
        _backgroundThreads = new BackgroundThread[n];
        for (int i = 0; i < n; ++i) {
            (_backgroundThreads[i] = new BackgroundThread(i + 1)).start();
        }
    }

    public void addTask(Runnable task) {
        _tasks.add(task);
    }

    public void finishAllAddedTasks() {
        try {
            _startBarrier.await();
            _startBarrier.reset();
            Runnable task;
            while ((task = _tasks.poll()) != null) {
                task.run();
            }
            _finishBarrier.await();
            _finishBarrier.reset();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
