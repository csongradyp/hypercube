package com.noe.hypercube.ui.tray.menu.list;

import com.noe.hypercube.ui.tray.menu.AbstractAnimatedListCell;
import com.noe.hypercube.ui.tray.menu.AnimatedListCell;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ListView;

import java.util.List;

public class FileListView<LIST_ITEM extends FileListItem> extends ListView<LIST_ITEM> {

    private Integer limit;

    public FileListView() {
        setPadding(Insets.EMPTY);
        setPrefSize(280, getFixedCellSize());
        setCellFactory(param -> new AnimatedListCell<>(AbstractAnimatedListCell.AnimationType.FADE_OUT));
    }

    public synchronized void clearAndSet(final List<LIST_ITEM> files) {
        setItems(FXCollections.observableArrayList(files));
    }

    public synchronized void add(final LIST_ITEM listItem) {
        Platform.runLater(() -> {
            final ObservableList<LIST_ITEM> items = getItems();
            if(limit != null) {
                if (items.size() == limit) {
                    items.remove(items.size() - 1);
                }
            }
            items.add(0, listItem);
            for (LIST_ITEM item : items) {
                item.refresh();
            }
        });
    }

    public void setLimit(final Integer limit) {
        this.limit = limit;
    }
}
