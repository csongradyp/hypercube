package com.noe.hypercube.ui;

import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.EventHandler;
import com.noe.hypercube.event.domain.FileEvent;
import com.noe.hypercube.event.domain.request.QueueContentRequest;
import com.noe.hypercube.event.domain.type.QueueType;
import com.noe.hypercube.event.domain.type.StreamDirection;
import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import com.noe.hypercube.ui.elements.AccountSegmentedButton;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import net.engio.mbassy.listener.Handler;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.noe.hypercube.event.domain.type.FileEventType.*;

public class SynchronizationQueueView extends VBox implements Initializable, EventHandler<FileEvent> {

    @FXML
    private AccountSegmentedButton accounts;
    @FXML
    private ListView<Label> downloadList;
    @FXML
    private ListView<Label> uploadList;

    public SynchronizationQueueView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("synchronizationQueueView.fxml"));
        fxmlLoader.setResources(ResourceBundle.getBundle("internationalization/messages", new Locale(ConfigurationBundle.getLanguage())));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        EventBus.subscribeToFileEvent(this);
        downloadList.setPlaceholder(createPlaceholder(AwesomeIcon.CLOUD_DOWNLOAD));
        uploadList.setPlaceholder(createPlaceholder(AwesomeIcon.CLOUD_UPLOAD));
        accounts.setOnAction(event -> EventBus.publish(new QueueContentRequest(QueueType.MAIN, accounts.getActiveAccount())));
        final ObservableList<ToggleButton> accountsButtons = accounts.getButtons();
        if(!accountsButtons.isEmpty()) {
            accountsButtons.get(0).fire();
        }
    }

    private Label createPlaceholder(AwesomeIcon icon) {
        final Label iconLabel = AwesomeDude.createIconLabel(icon, "", "150px", "12", ContentDisplay.TOP);
        iconLabel.getGraphic().setOpacity(0.1d);
        return iconLabel;
    }

    @Override
    @Handler(rejectSubtypes = true)
    public void onEvent(FileEvent event) {
        Platform.runLater(() -> {
            if (StreamDirection.DOWN == event.getDirection()) {
                handle(event, downloadList.getItems());
            } else {
                handle(event, uploadList.getItems());
            }
        });
    }

    private void handle(FileEvent event, ObservableList<Label> items) {
        if (SUBMITTED == event.getEventType()) {
            addListItem(event, items);
        } else {
            updateItems(event, items);
        }
    }

    private void addListItem(FileEvent event, ObservableList<Label> items) {
        Label item = AwesomeDude.createIconLabel(AwesomeIcon.CLOCK_ALT, event.getLocalPath().toString(), "15", "12", ContentDisplay.LEFT);
        items.add(0, item);
    }

    private void updateItems(FileEvent event, ObservableList<Label> items) {
        for (Label label : items) {
            if(label.getText().equals(event.getLocalPath().toString())) {
                if (STARTED == event.getEventType()) {
                    AwesomeDude.setIcon(label, AwesomeIcon.REFRESH);
                } else if (FINISHED == event.getEventType()) {
                    items.remove(label);
                }
                break;
            }
        }
    }

}
