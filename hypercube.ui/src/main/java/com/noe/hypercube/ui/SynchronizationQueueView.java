package com.noe.hypercube.ui;

import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.EventHandler;
import com.noe.hypercube.event.domain.FileEvent;
import com.noe.hypercube.event.domain.request.QueueContentRequest;
import com.noe.hypercube.event.domain.type.QueueType;
import com.noe.hypercube.event.domain.type.StreamDirection;
import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import com.noe.hypercube.ui.tray.menu.list.DetailedFileListItem;
import com.noe.hypercube.ui.tray.menu.list.ProcessedDetailedFileListItem;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import de.jensd.fx.fontawesome.AwesomeIconsStack;
import de.jensd.fx.fontawesome.Icon;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;
import net.engio.mbassy.listener.Handler;

import java.net.URL;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import static com.noe.hypercube.event.domain.type.FileEventType.*;

public class SynchronizationQueueView extends FileEventListView implements Initializable, EventHandler<FileEvent> {

    private final ResourceBundle resourceBundle;

    public SynchronizationQueueView() {
        resourceBundle = ResourceBundle.getBundle("internationalization/messages", new Locale(ConfigurationBundle.getLanguage()));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        EventBus.subscribeToFileEvent(this);
        accounts.setOnAction(event -> EventBus.publish(new QueueContentRequest(QueueType.MAIN, accounts.getActiveAccount())));
        final ObservableList<ToggleButton> accountsButtons = accounts.getButtons();
        if (!accountsButtons.isEmpty()) {
            accountsButtons.get(0).fire();
        }
    }

    @Override
    protected ObservableList<FileEvent> getListSource(final String account) {
        return null;
    }

    @Override
    protected StackPane getDownloadListPlaceholderIcon() {
        final AwesomeIconsStack iconsStack = AwesomeIconsStack.create();
        iconsStack.add(new Icon(AwesomeIcon.CLOUD_DOWNLOAD, "150px", "", ""));
        return iconsStack;
    }

    @Override
    protected StackPane getUploadListPlaceholder() {
        final AwesomeIconsStack iconsStack = AwesomeIconsStack.create();
        iconsStack.add(new Icon(AwesomeIcon.CLOUD_DOWNLOAD, "150px", "", ""));
        return iconsStack;
    }

    @Override
    @Handler(rejectSubtypes = true)
    public void onEvent(final FileEvent event) {
        Platform.runLater(() -> {
            if (StreamDirection.DOWN == event.getDirection()) {
                handle(event, downloadList.getItems());
            } else {
                handle(event, uploadList.getItems());
            }
        });
    }

    private void handle(final FileEvent event, final ObservableList<DetailedFileListItem> items) {
        if (SUBMITTED == event.getEventType()) {
            addListItem(event, items);
        } else {
            updateItems(event, items);
        }
    }

    private void addListItem(final FileEvent event, final ObservableList<DetailedFileListItem> items) {
        DetailedFileListItem item = new ProcessedDetailedFileListItem(event, resourceBundle);
        items.add(0, item);
    }

    private void updateItems(final FileEvent event, final ObservableList<DetailedFileListItem> items) {
        final Optional<DetailedFileListItem> match = items.parallelStream().filter(item -> item.getFileEvent().getLocalPath().equals(event.getLocalPath())
                        && item.getFileEvent().getRemotePath().equals(event.getRemotePath())
                        && item.getFileEvent().getActionType().equals(event.getActionType())
        ).findAny();

        if (match.isPresent()) {
            final DetailedFileListItem itemToUpdate = match.get();
            final FileEvent itemEvent = itemToUpdate.getFileEvent();
            if (itemEvent.getLocalPath().equals(event.getLocalPath()) && itemEvent.getRemotePath().equals(event.getRemotePath()) && itemEvent.getActionType().equals(event.getActionType())) {
                if (STARTED == event.getEventType()) {
                    itemToUpdate.setStatusIcon(AwesomeDude.createIconLabel(AwesomeIcon.REFRESH, "20"));
                } else if (FINISHED == event.getEventType()) {
                    items.remove(itemToUpdate);
                }
            }
        }
    }

}
