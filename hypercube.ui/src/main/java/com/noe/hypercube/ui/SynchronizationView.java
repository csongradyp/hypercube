package com.noe.hypercube.ui;

import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.EventHandler;
import com.noe.hypercube.event.domain.FileEvent;
import com.noe.hypercube.event.domain.type.StreamDirection;
import com.noe.hypercube.ui.bundle.AccountBundle;
import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import com.noe.hypercube.ui.bundle.HistoryBundle;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import net.engio.mbassy.listener.Handler;
import org.controlsfx.control.SegmentedButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.noe.hypercube.event.domain.type.FileEventType.*;

public class SynchronizationView extends AnchorPane implements EventHandler<FileEvent> {

    @FXML
    private SegmentedButton accounts;
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

    private void createAccountButtons() {
        final List<String> accountNames = AccountBundle.getAccounts();
        for (String account : accountNames) {
            final ToggleButton accountButton = new ToggleButton(account);
            accountButton.setFocusTraversable(false);
            accountButton.setPrefHeight(accounts.getPrefHeight());
            final ObservableList<FileEvent> fileEvents = HistoryBundle.getLastSyncedFiles().get(account);
            accountButton.setOnAction(e -> {
                accountButton.setSelected(true);
            });
            accounts.getButtons().add(accountButton);
        }
        accounts.getButtons().get(0).setSelected(true);
    }

    private void setDownloadListPlaceholder() {
        final Label iconLabel = AwesomeDude.createIconLabel(AwesomeIcon.CLOUD_DOWNLOAD);
        iconLabel.getGraphic().setOpacity(0.3d);
        downloadList.setPlaceholder(iconLabel);
    }

    private void setUploadListPlaceholder() {
        final Label iconLabel = AwesomeDude.createIconLabel(AwesomeIcon.CLOUD_UPLOAD);
        iconLabel.getGraphic().setOpacity(0.3d);
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
