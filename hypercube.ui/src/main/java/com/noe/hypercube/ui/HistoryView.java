package com.noe.hypercube.ui;

import com.noe.hypercube.event.domain.FileEvent;
import com.noe.hypercube.ui.bundle.HistoryBundle;
import de.jensd.fx.fontawesome.AwesomeIcon;
import de.jensd.fx.fontawesome.AwesomeIconsStack;
import de.jensd.fx.fontawesome.Icon;
import javafx.collections.ObservableList;
import javafx.scene.layout.StackPane;

public class HistoryView extends FileEventListView {

    @Override
    protected ObservableList<FileEvent> getListSource(String account) {
        return HistoryBundle.getLastSyncedFiles().get(account);
    }

    @Override
    protected StackPane getDownloadListPlaceholderIcon() {
        final AwesomeIconsStack iconsStack = AwesomeIconsStack.create();
        iconsStack.add(new Icon(AwesomeIcon.CLOUD_DOWNLOAD, "150px", "", "")).add(new Icon(AwesomeIcon.HISTORY, "70", "-fx-text-fill: blue;", ""));
        return iconsStack;
    }

    @Override
    protected StackPane getUploadListPlaceholder() {
        final AwesomeIconsStack iconsStack = AwesomeIconsStack.create();
        iconsStack.add(new Icon(AwesomeIcon.CLOUD_UPLOAD, "150px", "", "")).add(new Icon(AwesomeIcon.HISTORY, "40", "", ""));
        return iconsStack;
    }
}
