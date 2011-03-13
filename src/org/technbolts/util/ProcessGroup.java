package org.technbolts.util;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ProcessGroup<T> {

    private final Lock lock = new ReentrantLock();
    private final Condition taskSpawnedOrDone = lock.newCondition();
    private final AtomicInteger comodifier = new AtomicInteger();
    private List<FutureTask<T>> futures = New.arrayList();
    private List<FutureTask<T>> spawned = New.arrayList();
    private ExecutorService executor;

    public ProcessGroup(ExecutorService executor) {
        super();
        this.executor = executor;
    }

    public void spawn(Callable<T> callable) {
        spawn(new InternalFutureTask(callable));
    }

    public void spawn(Runnable callable) {
        spawn(new InternalFutureTask(callable, (T) null));
    }

    private void spawn(FutureTask<T> f) {
        lock.lock();
        try {
            spawned.add(f);
            executor.execute(f);
            taskSpawnedOrDone();
        }
        finally {
            lock.unlock();
        }
    }

    private void taskSpawnedOrDone() {
        lock.lock();
        try {
            comodifier.incrementAndGet();
            taskSpawnedOrDone.signal();
        }
        finally {
            lock.unlock();
        }
    }

    public void awaitTermination() throws InterruptedException {
        boolean done = false;
        try {
            do {
                int comod = comodifier.get();
                for (int i = futures.size() - 1; i >= 0; i--) {
                    FutureTask<T> f = futures.get(i);
                    if (!f.isDone()) {
                        f.run();
                    }
                    if(f.isDone())
                        futures.remove(i);
                }
                
                lock.lock();
                try {
                    futures.addAll(spawned);
                    spawned.clear();
                    
                    // not empty and tasks still runnings
                    if(!futures.isEmpty() && comod==comodifier.get()) {
                        taskSpawnedOrDone.await();
                    }
                }
                finally{
                    lock.unlock();
                }
            }
            while(!futures.isEmpty());

            done = true;
        } finally {
            if (!done)
                for (Future<T> f : futures)
                    f.cancel(true);
        }
    }
    
    class InternalFutureTask extends FutureTask<T> {
        
        private InternalFutureTask(Callable<T> callable) {
            super(callable);
        }

        private InternalFutureTask(Runnable runnable, T result) {
            super(runnable, result);
        }

        @Override
        protected void done() {
            super.done();
            taskSpawnedOrDone();
        }
    }

}
