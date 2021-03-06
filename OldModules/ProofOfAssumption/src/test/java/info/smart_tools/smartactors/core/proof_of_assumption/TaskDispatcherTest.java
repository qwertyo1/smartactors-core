package info.smart_tools.smartactors.core.proof_of_assumption;


import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import info.smart_tools.smartactors.task.interfaces.itask_dispatcher.ITaskDispatcher;
import info.smart_tools.smartactors.task.interfaces.ithread_pool.IThreadPool;
import info.smart_tools.smartactors.task.non_blocking_queue.NonBlockingQueue;
import info.smart_tools.smartactors.task.task_dispatcher.TaskDispatcher;
import info.smart_tools.smartactors.task.thread_pool.ThreadPool;
import org.junit.Test;

import java.text.MessageFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Performance test for {@link TaskDispatcher} and related components.
 */
public class TaskDispatcherTest {
    @Test
    public void test_taskQueuePerformance()
            throws Exception {
        ConcurrentLinkedQueue<ITask> innerTaskQueue = new ConcurrentLinkedQueue<>();
        IQueue<ITask> taskQueue = new NonBlockingQueue<>(innerTaskQueue);
//        IQueue<ITask> taskQueue = new BlockingQueue<>(new ArrayBlockingQueue<>(20000500));
        IThreadPool threadPool = new ThreadPool(8);
        ITaskDispatcher dispatcher = new TaskDispatcher(taskQueue, threadPool, 1000L, 8);
        final Thread mainThread = Thread.currentThread();
        final AtomicLong startNanoTime = new AtomicLong();
        final AtomicBoolean done = new AtomicBoolean(false);
        ConcurrentMap<Long, Long> threadUseCount = new ConcurrentHashMap<>();

        ITask countTask = () -> {
            Long tid = Thread.currentThread().getId();
            threadUseCount.put(tid, threadUseCount.computeIfAbsent(tid, l -> 0L) + 1);
        };

        ITask putTask = () -> {
            while (!mainThread.isInterrupted() && !Thread.interrupted()) {
                try {
                    taskQueue.put(countTask);
                    Thread.yield();
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
        for (int i = 0; i < 20000000; i++) {
            taskQueue.put(countTask);
        }

        taskQueue.put(() -> {
            long deltaTime = System.nanoTime() - startNanoTime.get();
            System.out.println(MessageFormat.format("Tasks handled in {0}ns ({1}s)", deltaTime, 0.000000001*(double)deltaTime));
            done.set(true);
            synchronized (done) {
                done.notifyAll();
            }
        });

        dispatcher.start();

        synchronized (done) {
            while (!done.get()) {
                done.wait();
            }
        }

        long totalTasks = 0;

        for (Long tid : threadUseCount.keySet()) {
            long nTasks = threadUseCount.get(tid);
            System.out.println(MessageFormat.format("T#{0}\t{1} tasks", tid, nTasks));
            totalTasks += nTasks;
        }

        System.out.println(MessageFormat.format("Total:\t{0} tasks", totalTasks));
    }
}
