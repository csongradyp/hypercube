package com.noe.hypercube.ui.bundle;

import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.EventHandler;
import com.noe.hypercube.event.domain.FileEvent;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.collections.ObservableList;
import net.engio.mbassy.listener.Handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class HistoryBundle implements EventHandler<FileEvent> {

    private final Map<String, ObservableList<FileEvent>> lastSyncedFiles;
    private final Map<String, ObservableList<FileEvent>> failedSyncedFiles;
    private static final Integer historySize = 1000;
    private static final HistoryBundle instance = new HistoryBundle();

    private HistoryBundle() {
        lastSyncedFiles = new HashMap<>();
        failedSyncedFiles = new HashMap<>();
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

    private void add(final FileEvent event, final Map<String, ObservableList<FileEvent>> map) {
        final ObservableList<FileEvent> fileEvents = map.get(event.getAccount());
        if (fileEvents.size() == historySize) {
            fileEvents.remove(fileEvents.size() - 1);
        }
        fileEvents.add(event);
    }

    public static Integer getHistorySize() {
        return historySize;
    }
}
