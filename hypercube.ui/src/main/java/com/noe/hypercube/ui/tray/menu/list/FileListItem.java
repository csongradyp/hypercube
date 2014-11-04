package com.noe.hypercube.ui.tray.menu.list;

import com.noe.hypercube.event.domain.FileEvent;
import com.noe.hypercube.ui.util.IconInjector;
import com.noe.hypercube.ui.util.LastSyncDisplayUtil;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;

import java.util.ResourceBundle;

public abstract class FileListItem extends HBox {

    protected final FileEvent fileEvent;
    protected final ResourceBundle messageBundle;
    protected final Label syncTime;

    public FileListItem(final FileEvent fileEvent, final ResourceBundle messageBundle) {
        super();
        this.messageBundle = messageBundle;
        this.fileEvent = fileEvent;
        syncTime = createTimeLabel(fileEvent);
        final Label streamDirection = IconInjector.getStreamDirectionIcon(fileEvent);
        final Label filePath = createFileLabel(fileEvent);
        setAlignment(Pos.CENTER_LEFT);
        getChildren().addAll(streamDirection, filePath, syncTime);
    }

    protected abstract Label createFileLabel(final FileEvent file);

    private Label createTimeLabel(final FileEvent file) {
        final Label syncTime = new Label(LastSyncDisplayUtil.convertToString(file.getTimeStamp(), messageBundle));
        syncTime.setTextAlignment(TextAlignment.RIGHT);
        syncTime.setAlignment(Pos.CENTER_RIGHT);
        syncTime.setPrefWidth(85d);
        return syncTime;
    }

    public void refresh() {
        syncTime.setText(LastSyncDisplayUtil.convertToString(fileEvent.getTimeStamp(), messageBundle));
    }

    public abstract FileListItem create(FileEvent file, ResourceBundle messageBundle);

    public FileEvent getFileEvent() {
        return fileEvent;
    }
}
