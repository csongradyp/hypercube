package com.noe.hypercube.ui.bundle;

import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.EventHandler;
import com.noe.hypercube.event.domain.FileEvent;
import com.noe.hypercube.event.domain.type.StreamDirection;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.collections.ObservableList;
import net.engio.mbassy.listener.Handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class HistoryBundle implements EventHandler<FileEvent> {

    public static final String LOCAL_STORAGE = "Local";
    private final Map<String, ObservableList<FileEvent>> lastSyncedFiles;
    private final Map<String, ObservableList<FileEvent>> failedSyncedFiles;
    private static final Integer historySize = 1000;
    private static final HistoryBundle instance = new HistoryBundle();

    private HistoryBundle() {
        lastSyncedFiles = new HashMap<>();
        failedSyncedFiles = new HashMap<>();
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

    public static void createSpaceFor(String account) {
        instance.lastSyncedFiles.put(account, new ObservableListWrapper<>(new ArrayList<>(historySize)));
        instance.failedSyncedFiles.put(account, new ObservableListWrapper<>(new ArrayList<>(historySize)));
    }

    @Override
    @Handler(rejectSubtypes = true)
    public void onEvent(final FileEvent event) {
        if (event.isFinished()) {
            add(event, lastSyncedFiles);
        } else if (event.isFailed()) {
            add(event, failedSyncedFiles);
        }
    }

    private void add(final FileEvent event, final Map<String, ObservableList<FileEvent>> trayHistoryMap) {
        ObservableList<FileEvent> fileEvents = trayHistoryMap.get(event.getAccount());
        if(event.getDirection() == StreamDirection.DOWN) {
            fileEvents = trayHistoryMap.get(LOCAL_STORAGE);
        }
        final Optional<FileEvent> sameItem = fileEvents.parallelStream().filter(listItem -> listItem.getDirection().equals(event.getDirection())
                && listItem.getLocalPath().equals(event.getLocalPath())
                && listItem.getRemotePath().equals(event.getRemotePath())
                && listItem.getActionType().equals(event.getActionType())
                && listItem.getAccount().equals(event.getAccount()))
                .findAny();
        if(sameItem.isPresent()) {
            fileEvents.remove(sameItem.get());
        }
        if (historySize == fileEvents.size()) {
            fileEvents.remove(fileEvents.size() - 1);
        }
        fileEvents.add(event);
    }

    public static Integer getHistorySize() {
        return historySize;
    }
}
