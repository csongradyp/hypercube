package com.noe.hypercube.ui;

import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.domain.request.MappingRequest;
import com.noe.hypercube.ui.bundle.AccountBundle;
import com.noe.hypercube.ui.bundle.PathBundle;
import com.noe.hypercube.ui.dialog.AddMappingDialog;
import com.noe.hypercube.ui.domain.account.AccountInfo;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;
import org.controlsfx.control.BreadCrumbBar;

public class MultiBreadCrumbBar extends VBox implements Initializable {

    private static final String SEPARATOR = System.getProperty("file.separator");
    private static final String SEPARATOR_PATTERN = Pattern.quote(SEPARATOR);
    private static final Pattern SLASH_SEPARATOR = Pattern.compile("/");

    private FileBreadCrumbBar localBreadcrumb;
    private Map<String, RemoteFileBreadCrumbBar> remoteBreadcrumbs;
    private EventHandler<BreadCrumbBar.BreadCrumbActionEvent<String>> remoteEventHandler;
    private EventHandler<BreadCrumbBar.BreadCrumbActionEvent<String>> localEventHandler;

    private SimpleBooleanProperty remote = new SimpleBooleanProperty(false);

    public MultiBreadCrumbBar() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("multiBreadCrumbBar.fxml"));
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
        remoteBreadcrumbs = new HashMap<>();
        localBreadcrumb = new FileBreadCrumbBar();
        localBreadcrumb.setActive(true);
        localBreadcrumb.setOnAddMapping(mouseEvent -> {
            final Optional<MappingRequest> request = AddMappingDialog.showMapLocalDialog(localBreadcrumb.getLocation());
            if(request.isPresent()) {
                EventBus.publishAddMappingRequest(request.get());
            }
        });
        getChildren().add(localBreadcrumb);
        setLocalCrumbActionHandler();
        final Collection<String> accounts = AccountBundle.getAccountNames();
        for (String account : accounts) {
            final RemoteFileBreadCrumbBar remoteBreadcrumb = new RemoteFileBreadCrumbBar(account);
            addRemoteCrumbEventHandler(remoteBreadcrumb);
            remoteBreadcrumb.setOnAddMapping(event -> AddMappingDialog.showMapRemoteDialog( remoteBreadcrumb.getAccount(), remoteBreadcrumb.getLocation()));
            setOnRemoveRemoteMapping(remoteBreadcrumb);
            remoteBreadcrumbs.put(account, remoteBreadcrumb);
        }
        addListenerForAccountChanges();
        localBreadcrumb.activeProperty().bind(remote.not());
    }

    private void addListenerForAccountChanges() {
        AccountBundle.getAccounts().addListener((ListChangeListener<AccountInfo>) change -> {
            while (change.next()) {
                final List<? extends AccountInfo> addedAccount = change.getAddedSubList();
                for (AccountInfo account : addedAccount) {
                    final RemoteFileBreadCrumbBar remoteBreadcrumb = new RemoteFileBreadCrumbBar(account.getName());
                    addRemoteCrumbEventHandler(remoteBreadcrumb);
                    remoteBreadcrumb.setOnAddMapping(event -> {
                        final Optional<MappingRequest> request = AddMappingDialog.showMapRemoteDialog(remoteBreadcrumb.getAccount(), remoteBreadcrumb.getLocation());
                        if(request.isPresent()) {
                            EventBus.publishAddMappingRequest(request.get());
                        }
                    });
                    setOnRemoveRemoteMapping(remoteBreadcrumb);
                    remoteBreadcrumbs.put(account.getName(), remoteBreadcrumb);
                }
                final List<? extends AccountInfo> removedAccount = change.getRemoved();
                for (AccountInfo account : removedAccount) {
                    //TODO handle removed accounts
                }
            }
        });
    }

    private void setOnRemoveRemoteMapping(final RemoteFileBreadCrumbBar remoteBreadcrumb) {
        remoteBreadcrumb.setOnRemoveMapping(event -> AddMappingDialog.showMapRemoteDialog(remoteBreadcrumb.getAccount(), remoteBreadcrumb.getLocation()));
    }

    private void addRemoteCrumbEventHandler(final RemoteFileBreadCrumbBar triggeredCrumbBar) {
        triggeredCrumbBar.setOnCrumbAction(event -> {
            remote.set(true);
            setAllRemoteCrumbsInactive();
            triggeredCrumbBar.setActive(true);
            if (remoteEventHandler != null) {
                remoteEventHandler.handle(event);
            }
        });
    }

    public Path getNewRemotePath(final BreadCrumbBar.BreadCrumbActionEvent<String> event, final String account) {
        final RemoteFileBreadCrumbBar remoteFileBreadCrumbBar = remoteBreadcrumbs.get(account);
        return Paths.get(account, remoteFileBreadCrumbBar.getPath(event).toString());
    }

    public Path getNewLocalPath(final BreadCrumbBar.BreadCrumbActionEvent<String> event) {
        String path = "";
        TreeItem<String> selectedCrumb = event.getSelectedCrumb();
        while (selectedCrumb != null) {
            path = selectedCrumb.getValue() + SEPARATOR + path;
            selectedCrumb = selectedCrumb.getParent();
        }
        return Paths.get(path);
    }

    private void setLocalCrumbActionHandler() {
        localBreadcrumb.setOnCrumbAction(event -> {
            setAllRemoteCrumbsInactive();
            localEventHandler.handle(event);
        });
    }

    public void setAllRemoteCrumbsInactive() {
        for (FileBreadCrumbBar fileBreadCrumbBar : remoteBreadcrumbs.values()) {
            fileBreadCrumbBar.setActive(false);
        }
    }

    public void setBreadCrumbs(final Path path) {
        setBreadCrumb(path.toString(), localBreadcrumb);
        final Map<String, String> remoteFolders = PathBundle.getAllRemoteFolders(path.toString());
        getChildren().clear();
        if (path.toFile().exists()) {
            getChildren().add(localBreadcrumb);
        }
        for (Map.Entry<String, String> folderMapping : remoteFolders.entrySet()) {
            final String account = folderMapping.getKey();
            final String remoteFolderPath = folderMapping.getValue();
            final RemoteFileBreadCrumbBar remoteBreadcrumb = remoteBreadcrumbs.get(account);
            setRemoteBreadCrumb(remoteFolderPath, account, remoteBreadcrumb);
            if (remoteBreadcrumb.isActive()) {
                getChildren().add(0, remoteBreadcrumb);
            } else {
                getChildren().add(remoteBreadcrumb);
            }
            remoteBreadcrumb.setCanAddMapping(false);
        }
    }

    public void setRemoteBreadCrumbs(final String account, final Path path) {
        if (account.equals("Cloud")) {
            getChildren().clear();
        } else {
            final RemoteFileBreadCrumbBar activeAccountCrumb = remoteBreadcrumbs.get(account);
            activeAccountCrumb.setActive(true);
            final String crumbPath = path == null || Paths.get(account).equals(path) ? "" : path.toString();
            final String localFolder = PathBundle.getLocalFolder(account, crumbPath);
            if (isMapped(localFolder)) {
                setBreadCrumbs(Paths.get(localFolder));
            } else {
                getChildren().clear();
                activeAccountCrumb.setCanAddMapping(true);
                setRemoteBreadCrumb(crumbPath, account, activeAccountCrumb);
                getChildren().add(activeAccountCrumb);
            }
        }
    }

    private boolean isMapped(final String localFolder) {
        return localFolder != null && !localFolder.isEmpty();
    }

    private void setBreadCrumb(String path, FileBreadCrumbBar breadcrumb) {
        TreeItem<String> model = BreadCrumbBar.buildTreeModel(path.split(SEPARATOR_PATTERN));
        if (model != null) {
            breadcrumb.setSelectedCrumb(model);
        }
    }

    private void setRemoteBreadCrumb(final String path, final String account, final RemoteFileBreadCrumbBar breadcrumb) {
        final Path pathWithAccount = Paths.get(account, path);
        final String breadcrumbPath = SLASH_SEPARATOR.matcher(pathWithAccount.toString()).replaceAll("\\\\");
        final TreeItem<String> model = BreadCrumbBar.buildTreeModel(breadcrumbPath.split(SEPARATOR_PATTERN));
        breadcrumb.setSelectedCrumb(model);
    }

    public RemoteFileBreadCrumbBar getActiveRemoteCrumb() {
        for (RemoteFileBreadCrumbBar remoteFileBreadCrumbBar : remoteBreadcrumbs.values()) {
            if(remoteFileBreadCrumbBar.isActive()) {
                return remoteFileBreadCrumbBar;
            }
        }
        return null;
    }

    public void setOnLocalCrumbAction(EventHandler<BreadCrumbBar.BreadCrumbActionEvent<String>> eventHandler) {
        localEventHandler = eventHandler;
    }

    public void setOnRemoteCrumbAction(EventHandler<BreadCrumbBar.BreadCrumbActionEvent<String>> remoteEventHandler) {
        this.remoteEventHandler = remoteEventHandler;
    }

    public SimpleBooleanProperty remoteProperty() {
        return remote;
    }

}
