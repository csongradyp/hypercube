package com.noe.hypercube.synchronization.queue;

import com.noe.hypercube.Action;
import com.noe.hypercube.domain.IStreamEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ManagedQueue<ENTRY extends IStreamEntry<DEPENDENCY>, DEPENDENCY> {

    private static final Logger LOG = LoggerFactory.getLogger(ManagedQueue.class);

    private final Collection<ENTRY> waitList;
    private final Collection<ENTRY> failList;
    private final BlockingQueue<ENTRY> mainQ;

    public ManagedQueue() {
        failList = new ArrayList<>();
        waitList = new ArrayList<>();
        mainQ = new LinkedBlockingDeque<>(100);
    }

    public void submit(final ENTRY entry) {
        if (entry.isDependent() && !exists(entry.getDependency())) {
            LOG.info("Dependency {} does not exist yet for entry {}", entry.getDependency(), entry);
            submitWait(entry);
        } else {
            Boolean foundSimilar = checkForSimilar(entry);
            if (!foundSimilar) {
                try {
                    mainQ.put(entry);
                } catch (InterruptedException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }

    private Boolean checkForSimilar(final ENTRY entry) {
        for (ENTRY queueEntry : mainQ) {
            if (queueEntry.equals(entry)) {
                final Action queueAction = queueEntry.getAction();
                final Action entryAction = entry.getAction();
                if (queueAction != entryAction) {
                    if (Action.ADDED == queueAction) {
                        if (entryAction == Action.CHANGED) {
                            LOG.info("Added file:{} was changed before streaming process", entry);
                        } else if (entryAction == Action.REMOVED) {
                            LOG.info("Added file:{} was removed before streaming process", entry);
                            mainQ.remove(queueEntry);
                        }
                    } else if (Action.REMOVED == queueAction) {
                        if (entryAction == Action.ADDED) {
                            LOG.info("A file with same name was added before deleting original: {} - action changed to UPDATE", queueEntry);
                            queueEntry.setAction(Action.CHANGED);
                        } else if (entryAction == Action.CHANGED) {
                            LOG.error("Inconsistency - Removed file could not be changed - deleted: {}, changed: {}", queueEntry, entry);
                        }
                    } else if (Action.CHANGED == queueAction) {
                        if (entryAction == Action.ADDED) {
                            LOG.error("Inconsistency - Changed file could not be added again - deleted: {}, changed: {}", queueEntry, entry);
                        } else if (entryAction == Action.REMOVED) {
                            // entry was removed meanwhile
                            LOG.info("Changed file:{} was deleted before streaming process", entry);
                            mainQ.remove(queueEntry);
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    public void submitFail(final ENTRY entry) {
        if (entry != null) {
            failList.add(entry);
        }
    }

    public void submitWait(final ENTRY entry) {
        waitList.add(entry);
        LOG.info("{} was added to wait list", entry);
    }

    public ENTRY take() throws InterruptedException {
        checkForDependencyFinished();
        // TODO observation of waiting finished dependencies before queue gets blocked
        return mainQ.take();
    }

    private void checkForDependencyFinished() {
        final List<ENTRY> readyEntries = waitList.parallelStream().filter(waiting -> exists(waiting.getDependency())).collect(Collectors.toList());
        mainQ.addAll(readyEntries);
        waitList.removeAll(readyEntries);
    }

    public void checkWaitings() {
        checkForDependencyFinished();
    }

    /**
     * Checks whether the dependency of the given entry exists. Should also check against {@code null}.
     *
     * @param dependency dependency of the waiting entry.
     * @return {@code true} if the dependency exist and is readable for further file process.
     */
    protected abstract boolean exists(DEPENDENCY dependency);

    public Collection<ENTRY> getWaitList() {
        return waitList;
    }

    public Collection<ENTRY> getFailList() {
        return failList;
    }

    public BlockingQueue<ENTRY> getMainQ() {
        return mainQ;
    }

}
