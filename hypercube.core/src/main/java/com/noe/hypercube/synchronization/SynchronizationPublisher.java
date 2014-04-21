package com.noe.hypercube.synchronization;


import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.domain.ServerEntry;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SynchronizationPublisher {

    private final Map<Class<? extends FileEntity>, EntryQueue<FileEntity>> upstreamQueues;
    private final Map<Class<? extends ServerEntry>, EntryQueue<ServerEntry>> downstreamQueues;

    @Inject
    private Collection<EntryQueue<FileEntity>> upstreamQueueCollection;
    @Inject
    private Collection<EntryQueue<ServerEntry>> downstreamQueueCollection;

    public SynchronizationPublisher() {
        upstreamQueues = new HashMap<>(10);
        downstreamQueues = new HashMap<>(10);
    }

    @PostConstruct
    private void createQueueMaps() {
        for (EntryQueue<FileEntity> queue : upstreamQueueCollection) {
            upstreamQueues.put(queue.getEntityClass(), queue);
        }
        for (EntryQueue<ServerEntry> queue : downstreamQueueCollection) {
            downstreamQueues.put(queue.getEntityClass(), queue);
        }
    }

    public boolean submitUpload(FileEntity entity) {
        EntryQueue<FileEntity> queue = upstreamQueues.get(entity.getClass());
        return queue.push(entity);
    }


}
