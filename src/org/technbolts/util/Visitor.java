package org.technbolts.util;

import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class Visitor<T,R> {
    private boolean isDone;
    private ConcurrentLinkedQueue<R> founds = New.concurrentLinkedQueue();
    
    public abstract void visit(T value);
    
    public boolean isDone () {
        return isDone;
    }
    
    public void done () {
        this.isDone = true;
    }
    
    public void add(R found) {
        this.founds.add(found);
    }
    
    public R getFirst() {
        if(founds.isEmpty())
            return null;
        return founds.peek();
    }
    
    public ConcurrentLinkedQueue<R> getFounds() {
        return founds;
    }
}