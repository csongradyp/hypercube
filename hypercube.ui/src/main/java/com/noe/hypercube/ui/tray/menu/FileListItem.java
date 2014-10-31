package com.noe.hypercube.ui.tray.menu;

import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.domain.FileEvent;
import com.noe.hypercube.event.domain.JumpToFileEvent;
import com.noe.hypercube.ui.util.FileManagerUtil;
import com.noe.hypercube.ui.util.IconInjector;
import com.noe.hypercube.ui.util.LastSyncDisplayUtil;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import java.util.ResourceBundle;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;

public class FileListItem extends HBox {

    private static final double HEIGHT = 40.0d;

    private final FileEvent fileEvent;
    private final ResourceBundle messageBundle;

    private Label syncTime;

    public FileListItem(final FileEvent fileEvent, final ResourceBundle messageBundle) {
        super();
        this.messageBundle = messageBundle;
        this.fileEvent = fileEvent;
        syncTime = createTimeLabel(fileEvent);
        final Label streamDirection = IconInjector.getStreamDirectionIcon(fileEvent);
        final Label filePath = createFileLabel(fileEvent);
        setAlignment(Pos.CENTER_LEFT);
        final Button viewButton = createViewButton(fileEvent);
        setOnMouseEntered(event -> viewButton.setVisible(true));
        setOnMouseExited(event -> viewButton.setVisible(false));
        setOnMouseClicked(event -> {
            if (isDoubleClick(event)) {
                FileManagerUtil.openFileManager(fileEvent.getLocalPath().toString());
            }
        });
        getChildren().addAll(streamDirection, filePath, viewButton, syncTime);
    }

    private boolean isDoubleClick(final MouseEvent event) {
        return event.getClickCount() == 2 && event.getButton().compareTo(MouseButton.PRIMARY) == 0;
    }

    private Label createFileLabel(final FileEvent file) {
        final Label fileName = new Label(file.getLocalPath().getFileName().toString());
        IconInjector.setFileStatusIcon(file, fileName);
        fileName.setPrefSize(150, HEIGHT);
        fileName.setAlignment(Pos.CENTER_LEFT);
        return fileName;
    }

    private Label createTimeLabel(final FileEvent file) {
        final Label syncTime = new Label(LastSyncDisplayUtil.convertToString(file.getTimeStamp(), messageBundle));
        syncTime.setTextAlignment(TextAlignment.RIGHT);
        syncTime.setAlignment(Pos.CENTER_RIGHT);
        syncTime.setPrefSize(85, HEIGHT);
        return syncTime;
    }

    private Button createViewButton(final FileEvent file) {
        final Button viewButton = new Button();
        viewButton.setPrefSize(15, 15);
        viewButton.setFocusTraversable(false);
        AwesomeDude.setIcon(viewButton, AwesomeIcon.MAP_MARKER);
        viewButton.setVisible(false);
        viewButton.setOnAction(mouseEvent -> EventBus.publish(new JumpToFileEvent(file.getLocalPath())));
        return viewButton;
    }

    public void refresh() {
        syncTime.setText(LastSyncDisplayUtil.convertToString(fileEvent.getTimeStamp(), messageBundle));
    }
}
