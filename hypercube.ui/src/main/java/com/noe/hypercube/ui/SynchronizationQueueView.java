package com.noe.hypercube.ui;

import com.noe.hypercube.event.domain.FileEvent;
import com.noe.hypercube.event.domain.type.StreamDirection;
import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import com.noe.hypercube.ui.bundle.HistoryBundle;
import com.noe.hypercube.ui.tray.menu.list.DetailedFileListItem;
import com.noe.hypercube.ui.tray.menu.list.ProcessedDetailedFileListItem;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import de.jensd.fx.fontawesome.AwesomeIconsStack;
import de.jensd.fx.fontawesome.Icon;
import java.util.*;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;

public class SynchronizationQueueView extends FileEventListView implements Initializable {

    private final ResourceBundle resourceBundle;

    public SynchronizationQueueView() {
        resourceBundle = ResourceBundle.getBundle("internationalization/messages", new Locale(ConfigurationBundle.getLanguage()));
    }

    @Override
    protected ObservableList<FileEvent> getListSource(final String account) {
        final Map<String, ObservableList<FileEvent>> submittedEvents = HistoryBundle.getSubmittedEvents();
        return submittedEvents.get(account);
    }

    @Override
    protected DetailedFileListItem createListItem(final FileEvent event) {
        final ProcessedDetailedFileListItem listItem = new ProcessedDetailedFileListItem(event, resourceBundle);
        if (event.isStarted()) {
            listItem.setStatusIcon(AwesomeDude.createIconLabel(AwesomeIcon.REFRESH, "20"));
        }
        return listItem;
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
        iconsStack.add(new Icon(AwesomeIcon.CLOUD_UPLOAD, "150px", "", ""));
        return iconsStack;
    }

    @Override
    protected void addListenerToListChanges(final String account) {
        getListSource(account).addListener((ListChangeListener<FileEvent>) change -> {
            if (account.equals(accounts.getActiveAccount())) {
                while (change.next()) {
                    change.getAddedSubList().forEach(event -> Platform.runLater(() -> {
                        if (StreamDirection.DOWN == event.getDirection()) {
                            handle(event, downloadList.getItems());
                        } else {
                            handle(event, uploadList.getItems());
                        }
                    }));
                }
            }
        });
    }

    private void handle(final FileEvent event, final Collection<DetailedFileListItem> items) {
        if (event.isSubmitted()) {
            addListItem(event, items);
        } else {
            updateItems(event, items);
        }
    }

    private void addListItem(final FileEvent event, final Collection<DetailedFileListItem> items) {
        DetailedFileListItem item = new ProcessedDetailedFileListItem(event, resourceBundle);
        items.add(item);
    }

    private Optional<DetailedFileListItem> findCorresponding(FileEvent event, Collection<DetailedFileListItem> fileEvents) {
        return fileEvents.stream().filter(listItem -> listItem.getFileEvent().getDirection().equals(event.getDirection())
                && listItem.getFileEvent().getLocalPath().equals(event.getLocalPath())
                && listItem.getFileEvent().getRemotePath().equals(event.getRemotePath())
                && listItem.getFileEvent().getActionType().equals(event.getActionType()))
                .findAny();
    }

    private void updateItems(final FileEvent event, final Collection<DetailedFileListItem> items) {
        final Optional<DetailedFileListItem> corresponding = findCorresponding(event, items);

        if (corresponding.isPresent()) {
            final DetailedFileListItem itemToUpdate = corresponding.get();
            itemToUpdate.refresh();
//            if (STARTED == event.getEventType()) {
//                itemToUpdate.setStatusIcon(AwesomeDude.createIconLabel(AwesomeIcon.REFRESH, "20"));
//            } else if (FINISHED == event.getEventType()) {
//                items.remove(itemToUpdate);
//            }
        }
    }

}
