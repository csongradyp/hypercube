package com.noe.hypercube.ui;

import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.domain.CreateFolderRequest;
import com.noe.hypercube.event.domain.DeleteRequest;
import com.noe.hypercube.event.domain.DownloadRequest;
import com.noe.hypercube.event.domain.UploadRequest;
import com.noe.hypercube.ui.action.FileAction;
import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import com.noe.hypercube.ui.dialog.FileActionConfirmDialog;
import com.noe.hypercube.ui.dialog.FileProgressDialog;
import com.noe.hypercube.ui.domain.file.IFile;
import com.noe.hypercube.ui.domain.file.RemoteFile;
import com.noe.hypercube.ui.elements.FileActionButton;
import com.noe.hypercube.ui.util.ProgressAwareInputStream;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.control.HiddenSidesPane;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialogs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import static com.noe.hypercube.ui.action.FileAction.*;
import static com.noe.hypercube.ui.util.PathConverterUtil.getAccount;
import static com.noe.hypercube.ui.util.PathConverterUtil.getEventPath;
import static org.controlsfx.dialog.Dialog.ACTION_YES;

public class FileManager extends VBox implements Initializable {

    @FXML
    private FileView leftFileView;
    @FXML
    private FileView rightFileView;

    @FXML
    private FileActionButton copy;
    @FXML
    private FileActionButton edit;
    @FXML
    private FileActionButton delete;
    @FXML
    private FileActionButton move;
    @FXML
    private FileActionButton newFolder;
    @FXML
    private FileActionButton close;
    @FXML
    private HiddenSidesPane doubleView;
    @FXML
    private AnchorPane syncView;
    @FXML
    private Label showSyncView;
    @FXML
    private Label hideSyncView;

    @FXML
    private ResourceBundle resources;

    private SimpleBooleanProperty cloud = new SimpleBooleanProperty(false);
    private SimpleBooleanProperty localToCloud = new SimpleBooleanProperty(false);
    private SimpleBooleanProperty cloudToLocal = new SimpleBooleanProperty(false);

    private SimpleBooleanProperty local = new SimpleBooleanProperty(false);

    public FileManager() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("fileManager.fxml"));
        ResourceBundle bundle = ResourceBundle.getBundle("internationalization/messages", new Locale(ConfigurationBundle.getLanguage()));
        fxmlLoader.setResources(bundle);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        resources = bundle;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        leftFileView.initStartLocation();
        rightFileView.initStartLocation();
        setupCloudCondition();
        setupLocalToCloudCondition();
        setupCloudToLocalCondition();
        setupLocalCondition();
        copy.prefWidthProperty().bind(widthProperty().subtract(40).divide(6));
        delete.prefWidthProperty().bind(widthProperty().subtract(40).divide(6));
        move.prefWidthProperty().bind(widthProperty().subtract(40).divide(6));
        edit.prefWidthProperty().bind(widthProperty().subtract(40).divide(6));
        newFolder.prefWidthProperty().bind(widthProperty().subtract(40).divide(6));
        close.prefWidthProperty().bind(widthProperty().subtract(40).divide(6));
        showSyncView.setOnMouseEntered(mouseEvent -> doubleView.setPinnedSide(Side.RIGHT));
        setupHideSyncViewLabel();
        syncView.prefWidthProperty().bind(rightFileView.widthProperty().add(20));
    }

    private void setupHideSyncViewLabel() {
        hideSyncView.setGraphic(AwesomeDude.createIconLabel(AwesomeIcon.PLAY, "", "10", "0", ContentDisplay.GRAPHIC_ONLY));
        hideSyncView.getGraphic().setStyle("-fx-effect: innershadow(gaussian, white, 7, 1, 1, 1);");
        hideSyncView.addEventHandler(MouseEvent.MOUSE_EXITED, event -> hideSyncView.setStyle("-fx-background-color: linear-gradient(to right, lightsteelblue 0%, #7A9ECC 50%, #5C92BF 51%, steelblue 100%);"));
        hideSyncView.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> hideSyncView.setStyle("-fx-background-color: linear-gradient(to right, lightsteelblue 0%, steelblue 40%, steelblue 80%, #3C76A6 100%);"));
        hideSyncView.setOnMouseClicked(mouseEvent -> doubleView.setPinnedSide(null));
    }

    private void setupLocalCondition() {
        local.bind(cloud.not().and(localToCloud.not()).and(cloudToLocal.not()));
        local.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                copy.setAction(COPY);
                move.setAction(MOVE);
                delete.setAction(DELETE);
                newFolder.setAction(NEW_FOLDER);
            }
        });
    }

    private void setupCloudToLocalCondition() {
        final BooleanProperty leftActive = leftFileView.getActiveProperty();
        final BooleanProperty rightActive = rightFileView.getActiveProperty();
        final SimpleBooleanProperty leftRemoteView = leftFileView.remoteProperty();
        final SimpleBooleanProperty rightRemoteView = rightFileView.remoteProperty();

        cloudToLocal.bind(((leftRemoteView.not().and(rightRemoteView)).and(rightActive))
                .or((leftRemoteView.and(rightRemoteView.not())).and(leftActive)));
        cloudToLocal.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                copy.setAction(DOWNLOAD);
                move.setAction(MOVE_DOWNLOAD);
                newFolder.setAction(NEW_CLOUD_FOLDER);
                delete.setAction(CLOUD_DELETE);
            }
        });
    }

    private void setupLocalToCloudCondition() {
        final BooleanProperty leftActive = leftFileView.getActiveProperty();
        final BooleanProperty rightActive = rightFileView.getActiveProperty();
        final SimpleBooleanProperty leftRemoteView = leftFileView.remoteProperty();
        final SimpleBooleanProperty rightRemoteView = rightFileView.remoteProperty();

        localToCloud.bind(((leftRemoteView.not().and(rightRemoteView)).and(leftActive))
                .or((leftRemoteView.and(rightRemoteView.not())).and(rightActive)));
        localToCloud.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                copy.setAction(UPLOAD);
                move.setAction(MOVE_UPLOAD);
            }
        });
    }

    private void setupCloudCondition() {
        cloud.bind(leftFileView.remoteProperty().and(rightFileView.remoteProperty()));
        cloud.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                copy.setAction(CLOUD_COPY);
                move.setAction(CLOUD_MOVE);
                delete.setAction(CLOUD_DELETE);
                newFolder.setAction(NEW_CLOUD_FOLDER);
            }
        });
    }

    @FXML
    public void onFileAction(final ActionEvent event) {
        final FileView activeFileView = getActiveFileView();
        final FileView inactiveFileView = getInactiveFileView();
        final Collection<IFile> markedFiles = activeFileView.getMarkedFiles();

        final FileAction action = getFileAction(event);
        final String title = getTitle(action);
        final Path sourceFolder = activeFileView.getLocation();
        final Path destinationFolder = inactiveFileView.getLocation();

        final Action response = new FileActionConfirmDialog(this, title, sourceFolder, destinationFolder, markedFiles).show();
        if (ACTION_YES == response) {
            if (isLocal(action)) {
                FileProgressDialog progressDialog = new FileProgressDialog(this, title, markedFiles.size(), sourceFolder, destinationFolder);
                processLocalFiles(progressDialog, markedFiles, action);
            } else if (isRemote(action)) {
                processRemoteFiles(markedFiles, action);
            } else if (isCross(action)) {
                processCrossFileAction(markedFiles, action);
            }
        } else {
            unMark(markedFiles);
        }
    }

    private FileAction getFileAction(final ActionEvent event) {
        final FileActionButton actionButton = (FileActionButton) event.getSource();
        return actionButton.getAction();
    }

    private boolean isLocal(final FileAction action) {
        return COPY == action || MOVE == action || DELETE == action;
    }

    private boolean isRemote(final FileAction action) {
        return CLOUD_COPY == action || CLOUD_MOVE == action || CLOUD_DELETE == action;
    }

    private boolean isCross(final FileAction action) {
        return UPLOAD == action || DOWNLOAD == action || MOVE_DOWNLOAD == action || MOVE_UPLOAD == action;
    }

    private void processLocalFiles(final FileProgressDialog progressDialog, final Collection<IFile> markedFiles, final FileAction action) {
        FileView inactiveFileView = getInactiveFileView();
        new Thread(() -> {
            for (IFile markedFile : markedFiles) {
                try {
                    if (COPY == action || MOVE == action) {
                        final Path destination = Paths.get(inactiveFileView.getLocation().toString(), markedFile.getName());
                        final ProgressAwareInputStream progressAwareInputStream = getProgressInputStream(markedFile);
                        Platform.runLater(() -> {
                            progressAwareInputStream.setOnProgressListener((percentage, tag) -> progressDialog.setProgress(percentage));
                            progressDialog.setProcessedFile(markedFile.getName());
                        });
                        Files.copy(progressAwareInputStream, destination, StandardCopyOption.REPLACE_EXISTING);
                        refreshViews(inactiveFileView, markedFile);
                    }
                    if (DELETE == action) {
                        Platform.runLater(() -> {
                            progressDialog.hideDestinationFolder();
                            progressDialog.setProcessedFile(markedFile.getName());
                            progressDialog.setProgress(1.0d);
                        });
                    }
                    if (DELETE == action || MOVE == action) {
                        if (markedFile.isDirectory()) {
                            Files.list(markedFile.getPath()).forEach(path -> {
                                try {
                                    Files.deleteIfExists(path);
                                } catch (IOException e) {
                                    Dialogs.create().message(resources.getString("dialog.delete.fail")).showError();
                                }
                            });
                            Files.deleteIfExists(markedFile.getPath());
                        } else {
                            Files.deleteIfExists(markedFile.getPath());
                        }
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            Platform.runLater(progressDialog::hide);
            getActiveFileView().refresh();
        }).start();
        Platform.runLater(progressDialog::show);
    }

    private void refreshViews(FileView inactiveFileView, IFile markedFile) {
        Platform.runLater(() -> {
            markedFile.setMarked(false);
            inactiveFileView.refresh();
            getActiveFileView().refresh();
        });
    }

    private void processRemoteFiles(final Collection<IFile> markedFiles, final FileAction action) {
        for (IFile markedFile : markedFiles) {
            if (RemoteFile.class.isAssignableFrom(markedFile.getClass())) {
                RemoteFile remoteFile = (RemoteFile) markedFile;
                if (CLOUD_COPY == action) {

                } else if (CLOUD_MOVE == action) {

                } else if (CLOUD_DELETE == action) {
                    final String remoteFileId = remoteFile.getId();
                    final String account = getAccount(getActiveFileView().getLocation());
                    if (remoteFileId != null) {
                        EventBus.publish(new DeleteRequest(account, remoteFileId));
                    } else {
                        EventBus.publish(new DeleteRequest(account, getEventPath(remoteFile.getPath())));
                    }
                }
            }
        }
    }

    private void processCrossFileAction(final Collection<IFile> markedFiles, final FileAction action) {
        final FileView inactiveFileView = getInactiveFileView();
        final String account = getAccount(inactiveFileView.getLocation());
        for (IFile markedFile : markedFiles) {
            if (UPLOAD == action) {
                EventBus.publish(new UploadRequest(account, markedFile.getPath(), getEventPath(inactiveFileView.getLocation())));
            } else if (DOWNLOAD == action) {
                EventBus.publish(new DownloadRequest(account, markedFile.getPath(), getEventPath(inactiveFileView.getLocation())));
            } else if (MOVE_UPLOAD == action) {
                EventBus.publish(new UploadRequest(account, markedFile.getPath(), getEventPath(inactiveFileView.getLocation()), true));
            } else if (MOVE_DOWNLOAD == action) {
                EventBus.publish(new DownloadRequest(account, markedFile.getPath(), getEventPath(inactiveFileView.getLocation()), true));
            }
        }
    }

    private ProgressAwareInputStream getProgressInputStream(final IFile markedFile) throws FileNotFoundException {
        final File file = markedFile.getPath().toFile();
        return new ProgressAwareInputStream(new FileInputStream(file), file.length(), null);
    }

    private void unMark(final Collection<IFile> markedFiles) {
        for (IFile markedFile : markedFiles) {
            markedFile.setMarked(false);
        }
    }

    private String getTitle(final FileAction action) {
        String titleKey = "";
        if (action == FileAction.COPY) {
            titleKey = "dialog.copy.title";
        } else if (action == FileAction.MOVE) {
            titleKey = "dialog.move.title";
        } else if (action == FileAction.DELETE) {
            titleKey = "dialog.delete.title";
        } else if (action == FileAction.NEW_FOLDER) {
            titleKey = "dialog.newfolder.title";
        } else if (action == FileAction.UPLOAD) {
            titleKey = "dialog.upload.title";
        } else if (action == FileAction.DOWNLOAD) {
            titleKey = "dialog.download.title";
        } else if (action == FileAction.MOVE_UPLOAD) {
            titleKey = "dialog.move.upload.title";
        } else if (action == FileAction.MOVE_DOWNLOAD) {
            titleKey = "dialog.move.download.title";
        } else if (action == FileAction.NEW_CLOUD_FOLDER) {
            titleKey = "dialog.newfolder.cloud.title";
        } else if (action == FileAction.CLOUD_COPY) {
            titleKey = "dialog.copy.cloud.title";
        } else if (action == FileAction.CLOUD_MOVE) {
            titleKey = "dialog.move.cloud.title";
        } else if (action == FileAction.CLOUD_DELETE) {
            titleKey = "dialog.delete.cloud.title";
        }
        return resources.getString(titleKey);
    }

    @FXML
    public void onNewFolder(final ActionEvent event) {
        final FileAction action = getFileAction(event);
        final Optional<String> folderName = Dialogs.create().title(getTitle(action)).showTextInput();
        final Path location = getActiveFileView().getLocation();
        if (folderName.isPresent()) {
            if (NEW_FOLDER == action) {
                try {
                    final Path dir = Paths.get(location.toString(), folderName.get());
                    Files.createDirectories(dir);
                } catch (IOException e1) {
                    Dialogs.create().message(resources.getString("dialog.newfolder.fail")).showError();
                }
            } else if (NEW_CLOUD_FOLDER == action) {
                EventBus.publish(new CreateFolderRequest(getAccount(location), getEventPath(location), folderName.get()));
            }
        }
    }

    @FXML
    public void onEdit(final ActionEvent e) {
        System.out.println(leftFileView.getLocation());
    }

    @FXML
    public void onExit() {
        ((Stage) getScene().getWindow()).close();
    }

    private FileView getActiveFileView() {
        if (leftFileView.isActive()) {
            return leftFileView;
        }
        return rightFileView;
    }

    private FileView getInactiveFileView() {
        if (!leftFileView.isActive()) {
            return leftFileView;
        }
        return rightFileView;
    }

}
