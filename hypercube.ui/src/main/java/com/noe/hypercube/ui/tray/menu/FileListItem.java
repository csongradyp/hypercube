package com.noe.hypercube.ui.tray.menu;

import com.noe.hypercube.event.domain.FileEvent;
import com.noe.hypercube.event.domain.FileEventType;
import com.noe.hypercube.ui.util.FileManagerUtil;
import com.noe.hypercube.ui.util.LastSyncDisplayUtil;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;

import java.util.ResourceBundle;

import static com.noe.hypercube.event.domain.FileEventType.*;

public class FileListItem extends HBox {

    private final ResourceBundle messageBundle;
    private final FileEvent fileEvent;
    private Label syncTime;

    public FileListItem(final FileEvent file, final ResourceBundle messageBundle) {
        super();
        this.messageBundle = messageBundle;
        this.fileEvent = file;
        syncTime = createTimeLabel(file);
        final Label filePath = createFileLabel(file);
        final Button viewButton = createShareButton(file);
        setAlignment(Pos.CENTER_LEFT);
        setOnMouseEntered(event -> viewButton.setVisible(true));
        setOnMouseExited(event -> viewButton.setVisible(false));
        setOnMouseClicked(event -> {
            if (isDoubleClick(event)) {
                FileManagerUtil.openFileManager(file.getLocalPath().toString());
            }
        });
        getChildren().addAll(filePath, viewButton, syncTime);
    }

    private boolean isDoubleClick(final MouseEvent event) {
        return event.getClickCount() == 2 && event.getButton().compareTo(MouseButton.PRIMARY) == 0;
    }

    private Label createFileLabel(final FileEvent file) {
        final Label fileName = new Label(file.getLocalPath().getFileName().toString());
        fileName.setPrefSize(170, 40);
        fileName.setAlignment(Pos.CENTER_LEFT);
        setStatusIcon(file, fileName);
        return fileName;
    }

    private void setStatusIcon(final FileEvent file, final Label fileName) {
        final FileEventType eventType = file.getEventType();
        if (NEW == eventType) {
            AwesomeDude.setIcon(fileName, AwesomeIcon.PLUS_CIRCLE);
        } else if (DELETED == eventType) {
            AwesomeDude.setIcon(fileName, AwesomeIcon.MINUS_CIRCLE);
        } else if (UPDATED == eventType) {
            AwesomeDude.setIcon(fileName, AwesomeIcon.CHECK_CIRCLE);
        }
    }

    private Label createTimeLabel(final FileEvent file) {
        final Label syncTime = new Label(LastSyncDisplayUtil.convertToString(file.getTimeStamp(), messageBundle));
        syncTime.setTextAlignment(TextAlignment.RIGHT);
        syncTime.setAlignment(Pos.CENTER_RIGHT);
        syncTime.setPrefSize(85, 40);
        return syncTime;
    }

    private Button createShareButton(final FileEvent file) {
        final Button viewButton = new Button();
        viewButton.setFocusTraversable(false);
        viewButton.setPrefSize(15, 15);
        AwesomeDude.setIcon(viewButton, AwesomeIcon.SHARE_ALT);
        viewButton.setVisible(false);
        viewButton.setOnAction(event -> {
            System.out.println(file.getLocalPath().getParent());
        });
        return viewButton;
    }

    public void refresh() {
        syncTime.setText(LastSyncDisplayUtil.convertToString(fileEvent.getTimeStamp(), messageBundle));
    }
}
