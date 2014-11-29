package com.noe.hypercube.ui;

import com.noe.hypercube.event.domain.FileEvent;
import com.noe.hypercube.event.domain.type.StreamDirection;
import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import com.noe.hypercube.ui.elements.AccountSegmentedButton;
import com.noe.hypercube.ui.tray.menu.list.DetailedFileListItem;
import com.noe.hypercube.ui.tray.menu.list.FileListView;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public abstract class FileEventListView extends VBox implements Initializable {

    @FXML
    protected AccountSegmentedButton accounts;
    @FXML
    protected FileListView<DetailedFileListItem> downloadList;
    @FXML
    protected FileListView<DetailedFileListItem> uploadList;
    private ResourceBundle resourceBundle;

    public FileEventListView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("fileEventListView.fxml"));
        fxmlLoader.setResources(ResourceBundle.getBundle("internationalization/messages", new Locale(ConfigurationBundle.getLanguage())));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        setUploadListPlaceholder();
        setDownloadListPlaceholder();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resourceBundle = resources;
        accounts.getButtons().get(0).fire();
        final ObservableList<ToggleButton> accountButtons = accounts.getButtons();
        for (ToggleButton accountButton : accountButtons) {
            accountButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                final String account = accounts.getActiveAccount();
                initializeListView(account);
                addListenerToListChanges(account);
            });
        }
    }

    private void addListenerToListChanges(final String account) {
        getListSource(account).addListener((ListChangeListener<FileEvent>) change -> {
            while (change.next()) {
                final List<? extends FileEvent> added = change.getAddedSubList();
                for (FileEvent event : added) {
                    Platform.runLater(() -> {
                        if (StreamDirection.DOWN == event.getDirection()) {
                            downloadList.add(new DetailedFileListItem(event, resourceBundle));
                        } else {
                            uploadList.add(new DetailedFileListItem(event, resourceBundle));
                        }
                    });
                }
            }
        });
    }

    private String initializeListView(final String account) {
        final List<DetailedFileListItem> downloads = new ArrayList<>();
        final List<DetailedFileListItem> uploads = new ArrayList<>();

        final ObservableList<FileEvent> accountSyncHistory = getListSource(account);
        for (FileEvent item : accountSyncHistory) {
            if(StreamDirection.DOWN == item.getDirection()) {
                downloads.add(new DetailedFileListItem(item, resourceBundle));
            } else {
                uploads.add(new DetailedFileListItem(item, resourceBundle));
            }
        }
        downloadList.clearAndSet(downloads);
        uploadList.clearAndSet(uploads);
        return account;
    }

    protected abstract ObservableList<FileEvent> getListSource(final String account);

    private void setDownloadListPlaceholder() {
        final StackPane icon = getDownloadListPlaceholderIcon();
        icon.setAlignment(Pos.CENTER);
        icon.setOpacity(0.1d);
        downloadList.setPlaceholder(icon);
    }

    protected abstract StackPane getDownloadListPlaceholderIcon();

    private void setUploadListPlaceholder() {
        final StackPane icon = getUploadListPlaceholder();
        icon.setAlignment(Pos.CENTER);
        icon.setOpacity(0.1d);
        uploadList.setPlaceholder(icon);
    }

    protected abstract StackPane getUploadListPlaceholder();
}
