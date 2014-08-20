package com.noe.hypercube.ui;

import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.domain.FileListRequest;
import com.noe.hypercube.ui.bundle.PathBundle;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.controlsfx.control.BreadCrumbBar;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

public class MultiBreadCrumbBar extends VBox implements Initializable {

    private static final String SEPARATOR = System.getProperty("file.separator");
    private static final String SEPARATOR_PATTERN = Pattern.quote(SEPARATOR);
    private static final Pattern SLASH_SEPARATOR = Pattern.compile("/");

    private PathBundle pathBundle;
    private FileBreadCrumbBar localBreadcrumb;
    private Map<String, RemoteFileBreadCrumbBar> remotebreadcrumbs;
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
        pathBundle = new PathBundle();
        remotebreadcrumbs = new HashMap<>();
        localBreadcrumb = new FileBreadCrumbBar(true);
        setLocalCrumbActionHandler();
        final Collection<String> accounts = pathBundle.getAccounts();
        for (String account : accounts) {
            final RemoteFileBreadCrumbBar remoteBreadcrumb = new RemoteFileBreadCrumbBar(account);
            setRemoteCrumbEventHandler(remoteBreadcrumb);
            remotebreadcrumbs.put(account, remoteBreadcrumb);
        }
        disableBreadcrumbFocusTraversal(localBreadcrumb);
        remotebreadcrumbs.values().forEach(this::disableBreadcrumbFocusTraversal);
        getChildren().add(localBreadcrumb);
        localBreadcrumb.activeProperty().bind(remote.not());
    }

    private void setRemoteCrumbEventHandler(FileBreadCrumbBar remote) {
        remote.setOnCrumbAction(event -> {
            if(remoteEventHandler != null) {
                remoteEventHandler.handle(event);
            }
            remoteProperty().set(true);
            final RemoteFileBreadCrumbBar triggeredCrumbBar = (RemoteFileBreadCrumbBar) event.getSource();
            final String account = triggeredCrumbBar.getAccount();
            final Path newPath = getNewRemotePath(event, account);
            setAllRemoteCrumbsInactive();
            triggeredCrumbBar.setActive(true);
            EventBus.publish(new FileListRequest(account, newPath));
        });
    }

    private Path getNewRemotePath(final BreadCrumbBar.BreadCrumbActionEvent<String> event, String account) {
        String path = "";
        TreeItem<String> selectedCrumb = event.getSelectedCrumb();
        while (selectedCrumb != null) {
            final String folder = selectedCrumb.getValue();
            if (folder.equals(account)) {
                path = "/" + path;
            } else {
                path = folder + "/" + path;
            }
            selectedCrumb = selectedCrumb.getParent();
        }
        return Paths.get(path);
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
        for (FileBreadCrumbBar fileBreadCrumbBar : remotebreadcrumbs.values()) {
            fileBreadCrumbBar.setActive(false);
        }
    }

    public void setBreadCrumbs(Path path) {
        setBreadCrumb(path.toString(), localBreadcrumb);
        final Map<String, String> remoteFolders = pathBundle.getAllFolders(path.toString());
        getChildren().clear();
        if (path.toFile().exists()) {
            getChildren().add(localBreadcrumb);
        }
        for (Map.Entry<String, String> entry : remoteFolders.entrySet()) {
            final String account = entry.getKey();
            final FileBreadCrumbBar remoteBreadcrumb = remotebreadcrumbs.get(account);
            final String folderPath = entry.getValue();
            setRemoteBreadCrumb(folderPath, account, remoteBreadcrumb);
            if(remoteBreadcrumb.isActive()) {
                getChildren().add(0,remoteBreadcrumb);
            } else {
                getChildren().add(remoteBreadcrumb);
            }
        }
    }

    public void setRemoteBreadCrumbs(String account, Path path) {
        final FileBreadCrumbBar activeAccountCrumb = remotebreadcrumbs.get(account);
        activeAccountCrumb.setActive(true);
        final String crumbPath = path == null ? "" : path.toString();
        final String localFolder = pathBundle.getLocalFolder(account, crumbPath);
        if(localFolder != null && !localFolder.isEmpty()) {
            setBreadCrumbs(Paths.get(localFolder));
        } else {
            getChildren().clear();
            setRemoteBreadCrumb(crumbPath, account, activeAccountCrumb);
            getChildren().add(activeAccountCrumb);
        }
    }

    private void setBreadCrumb(String path, BreadCrumbBar<String> breadcrumb) {
        TreeItem<String> model = BreadCrumbBar.buildTreeModel(path.split(SEPARATOR_PATTERN));
        if (model != null) {
            breadcrumb.setSelectedCrumb(model);
        }
    }

    private void setRemoteBreadCrumb(final String path, final String account, final BreadCrumbBar<String> breadcrumb) {
        String breadcrumbPath = account + path;
        breadcrumbPath = SLASH_SEPARATOR.matcher(breadcrumbPath).replaceAll("\\\\");
        final TreeItem<String> model = BreadCrumbBar.buildTreeModel(breadcrumbPath.split(SEPARATOR_PATTERN));
        breadcrumb.setSelectedCrumb(model);
    }

    private void disableBreadcrumbFocusTraversal(BreadCrumbBar<String> breadcrumb) {
        breadcrumb.setFocusTraversable(false);
        Callback<TreeItem<String>, Button> crumbFactory = breadcrumb.getCrumbFactory();
        breadcrumb.setCrumbFactory((param) -> {
            Button crumbButton = crumbFactory.call(param);
            crumbButton.setFocusTraversable(false);
            return crumbButton;
        });
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
