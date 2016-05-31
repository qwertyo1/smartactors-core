package info.smart_tools.smartactors.core.proof_of_assumption;


import info.smart_tools.smartactors.core.blocking_queue.BlockingQueue;
import info.smart_tools.smartactors.core.iqueue.IQueue;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.ithread_pool.IThreadPool;
import info.smart_tools.smartactors.core.task_queue_listener.MasterTaskQueueListener;
import info.smart_tools.smartactors.core.thread_pool.ThreadPool;
import org.junit.Test;

import java.text.MessageFormat;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Performance test for {@link MasterTaskQueueListener} and related components.
 */
public class TaskQueueListeners {
    @Test
    public void test_taskQueuePerformance()
            throws Exception {
        IQueue<ITask> taskQueue = new BlockingQueue<>(new ArrayBlockingQueue<>(10000500));
        IThreadPool threadPool = new ThreadPool(8);
        ITask listeningTask = new MasterTaskQueueListener(taskQueue, threadPool);
        final Thread mainThread = Thread.currentThread();
        final AtomicLong startNanoTime = new AtomicLong();
        ConcurrentMap<Long, Long> threadUseCount = new ConcurrentHashMap<>();

        ITask countTask = () -> {
            Long tid = Thread.currentThread().getId();
            threadUseCount.put(tid, threadUseCount.computeIfAbsent(tid, l -> 0L) + 1);
        };

        ITask putTask = () -> {
            while (!mainThread.isInterrupted() && !Thread.interrupted()) {
                try {
                    taskQueue.put(countTask);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        // Warm up
        for (int i = 0; i < 100; i++) {
            taskQueue.put(countTask);
        }

        taskQueue.put(putTask);
        taskQueue.put(putTask);
        taskQueue.put(putTask);
        taskQueue.put(putTask);

        // Start measurement
        taskQueue.put(() -> startNanoTime.set(System.nanoTime()));

        // Many tasks
        for (int i = 0; i < 10000000; i++) {
            taskQueue.put(countTask);
        }

        taskQueue.put(() -> {
            long deltaTime = System.nanoTime() - startNanoTime.get();
            System.out.println(MessageFormat.format("Tasks handled in {0}ns ({1}s)", deltaTime, 0.000000001*(double)deltaTime));
            mainThread.interrupt();
        });

        listeningTask.execute();

        long totalTasks = 0;

        for (Long tid : threadUseCount.keySet()) {
            long nTasks = threadUseCount.get(tid);
            System.out.println(MessageFormat.format("T#{0}\t{1} tasks", tid, nTasks));
            totalTasks += nTasks;
        }

        System.out.println(MessageFormat.format("Total:\t{0} tasks", totalTasks));
    }
}
