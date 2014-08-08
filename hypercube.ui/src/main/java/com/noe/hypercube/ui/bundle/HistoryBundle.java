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
    private static final HistoryBundle instance = new HistoryBundle();
    private static final Integer historySize = 100;

    private HistoryBundle() {
        lastSyncedFiles = new HashMap<>();
        EventBus.subscribeToFileEvent(this);
    }

    public static Map<String, ObservableList<FileEvent>> getLastSyncedFiles() {
        return instance.lastSyncedFiles;
    }

    public static void createSpaceFor(String account) {
        instance.lastSyncedFiles.put(account, new ObservableListWrapper<>(new ArrayList<>(6)));
    }

    @Override
    @Handler(rejectSubtypes = true)
    public void onEvent(final FileEvent event) {
        if (event.isFinished()) {
            final ObservableList<FileEvent> fileEvents = lastSyncedFiles.get(event.getAccountName());
            if (fileEvents.size() == historySize) {
                fileEvents.remove(fileEvents.size() - 1);
            }
            fileEvents.add(event);
        }
    }

    public static Integer getHistorySize() {
        return historySize;
    }
}
