package com.noe.hypercube.synchronization;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class EntryQueue<ENTITY_TYPE> {

    private BlockingQueue<ENTITY_TYPE> queue;

    public abstract Class<ENTITY_TYPE> getEntityClass();

    public EntryQueue() {
        this.queue = new LinkedBlockingQueue<>();
    }

    public boolean push(ENTITY_TYPE entity) {
        return queue.add(entity);
    }

    public ENTITY_TYPE pop() {
        return queue.poll();
    }

    public boolean contains(ENTITY_TYPE entity) {
        return queue.contains(entity);
    }
}
