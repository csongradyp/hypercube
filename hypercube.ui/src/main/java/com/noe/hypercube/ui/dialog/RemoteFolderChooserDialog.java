package com.noe.hypercube.ui.dialog;

import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.EventHandler;
import com.noe.hypercube.event.domain.request.FileListRequest;
import com.noe.hypercube.event.domain.response.FileListResponse;
import com.noe.hypercube.ui.RemoteFileBreadCrumbBar;
import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import com.noe.hypercube.ui.domain.file.IFile;
import com.noe.hypercube.ui.domain.file.RemoteFile;
import com.noe.hypercube.ui.factory.IconFactory;
import com.noe.hypercube.ui.util.StyleUtil;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import net.engio.mbassy.listener.Handler;
import org.controlsfx.control.BreadCrumbBar;


public class RemoteFolderChooserDialog extends Dialog<Path> implements Initializable, EventHandler<FileListResponse> {

    private static final String SEPARATOR = System.getProperty("file.separator");
    private static final String SEPARATOR_PATTERN = Pattern.quote(SEPARATOR);
    private static final Pattern SLASH_SEPARATOR = Pattern.compile("/");

    private final KeyCombination enter = new KeyCodeCombination(KeyCode.ENTER);
    private final KeyCombination back = new KeyCodeCombination(KeyCode.BACK_SPACE);

    @FXML
    private ListView<RemoteFile> folderListView;
    @FXML
    private VBox content;

    private final String account;
    private final RemoteFileBreadCrumbBar folderBreadcrumb;

    public RemoteFolderChooserDialog(final String account) {
        this.account = account;
        folderBreadcrumb = new RemoteFileBreadCrumbBar(account);
        final ResourceBundle resourceBundle = ResourceBundle.getBundle("internationalization/messages", new Locale(ConfigurationBundle.getLanguage()));
        FXMLLoader fxmlLoader = new FXMLLoader(BindManagerDialog.class.getClassLoader().getResource("remoteFolderChooserDialog.fxml"), resourceBundle);
        fxmlLoader.setResources(resourceBundle);
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
        manageSubscriptions();
        setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return folderListView.getSelectionModel().getSelectedItem().getPath();
            }
            return null;
        });
        StyleUtil.changeStyle(getDialogPane(), account);
        setCellFactory();
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        initializeBreadCrumb();
        EventBus.publish(new FileListRequest(folderListView.hashCode(), account, null, null));
    }

    private void initializeBreadCrumb() {
        folderBreadcrumb.setActive(true);
        folderBreadcrumb.removeMappingButton();
        folderBreadcrumb.setOnCrumbAction(event -> {
            final Path newPath = folderBreadcrumb.getPath(event);
            EventBus.publish(new FileListRequest(folderListView.hashCode(), account, newPath, folderBreadcrumb.getLocation()));
        });
        content.getChildren().add(0, folderBreadcrumb);
    }

    private void manageSubscriptions() {
        setOnShowing(event -> EventBus.subscribeToFileListResponse(RemoteFolderChooserDialog.this));
        setOnHiding(event -> EventBus.unsubscribeToFileListResponse(RemoteFolderChooserDialog.this));
    }

    private void setCellFactory() {
        folderListView.setCellFactory(new Callback<ListView<RemoteFile>, ListCell<RemoteFile>>() {
            @Override
            public ListCell<RemoteFile> call(ListView<RemoteFile> param) {
                final ListCell<RemoteFile> cell = new ListCell<RemoteFile>() {
                    @Override
                    protected void updateItem(RemoteFile file, boolean empty) {
                        super.updateItem(file, empty);
                        if (file != null && !empty) {
                            setGraphic(IconFactory.getFileIcon(file));
                        } else {
                            setGraphic(null);
                        }
                    }
                };
                cell.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                    if (event.getClickCount() == 2) {
                        final RemoteFile remoteFile = cell.getItem();
                        EventBus.publish(new FileListRequest(folderListView.hashCode(), account, remoteFile.getPath(), remoteFile.getParentDirectory()));
                    }
                });
                return cell;
            }
        });
    }

    @FXML
    public void onKeyPressed(final KeyEvent event) {
        final IFile remoteFile = folderListView.getSelectionModel().getSelectedItem();
        if (enter.match(event)) {
            EventBus.publish(new FileListRequest(folderListView.hashCode(), account, remoteFile.getPath(), remoteFile.getParentDirectory()));
        } else if (back.match(event)) {
            EventBus.publish(new FileListRequest(folderListView.hashCode(), account, folderBreadcrumb.getLocation().getParent(), folderBreadcrumb.getLocation()));
        }
    }

    @Override
    @Handler(rejectSubtypes = true)
    public void onEvent(final FileListResponse event) {
        if (event.getTarget().equals(folderListView.hashCode())) {
            final Collection<RemoteFile> remoteFiles = new ArrayList<>();
            final List<ServerEntry> fileList = event.getFileList();
            if (fileList != null) {
                for (ServerEntry serverEntry : fileList) {
                    if (serverEntry.isFolder()) {
                        remoteFiles.add(new RemoteFile(serverEntry.getPath(), 0L, serverEntry.isFolder(), serverEntry.lastModified()));
                    }
                }
                Platform.runLater(() -> {
                    folderListView.setItems(FXCollections.observableArrayList(remoteFiles));
                    setBreadCrumb(event.getFolder().toString());
                });
            }
        }
    }

    private void setBreadCrumb(final String path) {
        final Path pathWithAccount = Paths.get(account, path);
        final String breadcrumbPath = SLASH_SEPARATOR.matcher(pathWithAccount.toString()).replaceAll("\\\\");
        final TreeItem<String> model = BreadCrumbBar.buildTreeModel(breadcrumbPath.split(SEPARATOR_PATTERN));
        folderBreadcrumb.setSelectedCrumb(model);
    }

}
