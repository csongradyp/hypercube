package com.noe.hypercube.ui.tray.menu;

import com.noe.hypercube.event.domain.FileEvent;
import com.noe.hypercube.ui.bundle.HistoryBundle;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ListView;

import java.util.List;
import java.util.ResourceBundle;

public class FileListView extends ListView<FileListItem> {

    private ResourceBundle messageBundle;

    public FileListView() {
        setPadding(Insets.EMPTY);
        setPrefSize(280, getFixedCellSize());
    }

    public synchronized void clearAndSet(final List<FileEvent> files) {
        getItems().clear();
        for (FileEvent file : files) {
            add(file);
        }
    }

    public synchronized void add(FileEvent file) {
        Platform.runLater(() -> {
            final ObservableList<FileListItem> items = getItems();
            if (items.size() == HistoryBundle.getHistorySize()) {
                items.remove(items.size() - 1);
            }
            items.add(0, new FileListItem(file, messageBundle));
            for (FileListItem item : items) {
                item.refresh();
            }
        });
    }

    public void setMessageBundle(ResourceBundle messageBundle) {
        this.messageBundle = messageBundle;
    }

}