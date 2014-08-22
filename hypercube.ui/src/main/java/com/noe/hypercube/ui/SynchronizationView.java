package com.noe.hypercube.ui;

import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.EventHandler;
import com.noe.hypercube.event.domain.FileEvent;
import com.noe.hypercube.event.domain.type.StreamDirection;
import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import com.noe.hypercube.ui.elements.AccountSegmentedButton;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import net.engio.mbassy.listener.Handler;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.noe.hypercube.event.domain.type.FileEventType.*;

public class SynchronizationView extends VBox implements EventHandler<FileEvent> {

    @FXML
    private AccountSegmentedButton accounts;
    @FXML
    private ListView<Label> downloadList;
    @FXML
    private ListView<Label> uploadList;

    public SynchronizationView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("syncView.fxml"));
        fxmlLoader.setResources(ResourceBundle.getBundle("internationalization/messages", new Locale(ConfigurationBundle.getLanguage())));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        EventBus.subscribeToFileEvent(this);
        setUploadListPlaceholder();
        setDownloadListPlaceholder();
    }

    private void setDownloadListPlaceholder() {
        final Label iconLabel = AwesomeDude.createIconLabel(AwesomeIcon.CLOUD_DOWNLOAD, "", "150px", "12", ContentDisplay.TOP);
        iconLabel.getGraphic().setOpacity(0.1d);
        downloadList.setPlaceholder(iconLabel);
    }

    private void setUploadListPlaceholder() {
        final Label iconLabel = AwesomeDude.createIconLabel(AwesomeIcon.CLOUD_UPLOAD, "", "150px", "12", ContentDisplay.TOP);
        iconLabel.getGraphic().setOpacity(0.1d);
        uploadList.setPlaceholder(iconLabel);
    }

    @Override
    @Handler(rejectSubtypes = true)
    public void onEvent(FileEvent event) {
        Label item = createListItem(event);
        if (event.getDirection() == StreamDirection.DOWN) {
            downloadList.getItems().add(item);
        } else {
            uploadList.getItems().add(item);
        }
    }

    private Label createListItem(final FileEvent event) {
        Label item = null;
        if (SUBMITTED == event.getEventType()) {
            item = AwesomeDude.createIconLabel(AwesomeIcon.CLOCK_ALT, event.getLocalPath().toString(), "15", "12", ContentDisplay.LEFT);
        } else if (STARTED == event.getEventType()) {
            item = AwesomeDude.createIconLabel(AwesomeIcon.REFRESH, event.getLocalPath().toString(), "15", "12", ContentDisplay.LEFT);
        } else if (FINISHED == event.getEventType()) {
            item = AwesomeDude.createIconLabel(AwesomeIcon.CHECK_SQUARE, event.getLocalPath().toString(), "15", "12", ContentDisplay.LEFT);
        }
        return item;
    }
}
