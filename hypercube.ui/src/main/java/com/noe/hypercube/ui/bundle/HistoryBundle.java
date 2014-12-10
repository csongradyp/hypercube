package com.noe.hypercube.ui.bundle;

import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.EventHandler;
import com.noe.hypercube.event.domain.FileEvent;
import com.sun.javafx.collections.ObservableListWrapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javafx.collections.ObservableList;
import net.engio.mbassy.listener.Handler;

public final class HistoryBundle implements EventHandler<FileEvent> {

    public static final String LOCAL_STORAGE = "Local";
    private final Map<String, ObservableList<FileEvent>> lastSyncedFiles;
    private final Map<String, ObservableList<FileEvent>> failedSyncedFiles;
    private final Map<String, ObservableList<FileEvent>> submittedEvents;
    private static final Integer historySize = 1000;
    private static final HistoryBundle instance = new HistoryBundle();

    private HistoryBundle() {
        lastSyncedFiles = new HashMap<>();
        failedSyncedFiles = new HashMap<>();
        submittedEvents = new HashMap<>();
        lastSyncedFiles.put(LOCAL_STORAGE, new ObservableListWrapper<>(new ArrayList<>(historySize)));
        failedSyncedFiles.put(LOCAL_STORAGE, new ObservableListWrapper<>(new ArrayList<>(historySize)));
        EventBus.subscribeToFileEvent(this);
    }

    public static Map<String, ObservableList<FileEvent>> getLastSyncedFiles() {
        return instance.lastSyncedFiles;
    }

    public static Map<String, ObservableList<FileEvent>> getFailedSyncedFiles() {
        return instance.failedSyncedFiles;
    }

    public static Map<String, ObservableList<FileEvent>> getSubmittedEvents() {
        return instance.submittedEvents;
    }

    public static void createSpaceFor(String account) {
        instance.lastSyncedFiles.put(account, new ObservableListWrapper<>(new ArrayList<>(historySize)));
        instance.failedSyncedFiles.put(account, new ObservableListWrapper<>(new ArrayList<>(historySize)));
        instance.submittedEvents.put(account, new ObservableListWrapper<>(new ArrayList<>(historySize)));
    }

    @Override
    @Handler(rejectSubtypes = true)
    public void onEvent(final FileEvent event) {
        if (event.isFinished()) {
            add(event, lastSyncedFiles);
        } else if (event.isFailed()) {
            add(event, failedSyncedFiles);
        }

        if(!event.isFailed()) {
//        add(event, submittedEvents);
            final ObservableList<FileEvent> fileEvents = submittedEvents.get(event.getAccount());
            if (historySize == fileEvents.size()) {
                fileEvents.remove(fileEvents.size() - 1);
            }
            final Optional<FileEvent> corresponding = findCorresponding(event, fileEvents);
            if (corresponding.isPresent()) {
                final FileEvent recordedEvent = corresponding.get();
                if(event.isFinished()) {
                    fileEvents.remove(recordedEvent);
                } if(event.isStarted()) {
                    recordedEvent.setStarted();
                }
            } else {
                fileEvents.add(0, event);
            }
        }
    }

    private void add(final FileEvent event, final Map<String, ObservableList<FileEvent>> eventMap) {
        ObservableList<FileEvent> fileEvents = eventMap.get(event.getAccount());
//        if (event.getDirection() == StreamDirection.DOWN) {
//            fileEvents = eventMap.get(LOCAL_STORAGE);
//        }
        removeSame(event, fileEvents);
        if (historySize == fileEvents.size()) {
            fileEvents.remove(fileEvents.size() - 1);
        }
        fileEvents.add(event);
    }

    private void removeSame(final FileEvent event, final ObservableList<FileEvent> fileEvents) {
        final Optional<FileEvent> sameItem = findCorresponding(event, fileEvents);
        if (sameItem.isPresent()) {
            fileEvents.remove(sameItem.get());
        }
    }

    private Optional<FileEvent> findCorresponding(FileEvent event, ObservableList<FileEvent> fileEvents) {
        return fileEvents.stream().filter(listItem -> listItem.getDirection().equals(event.getDirection())
                    && listItem.getLocalPath().equals(event.getLocalPath())
                    && listItem.getRemotePath().equals(event.getRemotePath())
                    && listItem.getActionType().equals(event.getActionType()))
                    .findAny();
    }

    public static Integer getHistorySize() {
        return historySize;
    }
}
