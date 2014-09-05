package com.noe.hypercube.ui;

import com.noe.hypercube.ui.bundle.PathBundle;
import com.noe.hypercube.ui.dialog.AddMappingDialog;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.controlsfx.control.BreadCrumbBar;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class MultiBreadCrumbBar extends VBox implements Initializable {

    private static final String SEPARATOR = System.getProperty("file.separator");
    private static final String SEPARATOR_PATTERN = Pattern.quote(SEPARATOR);
    private static final Pattern SLASH_SEPARATOR = Pattern.compile("/");

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
        remotebreadcrumbs = new HashMap<>();
        localBreadcrumb = new FileBreadCrumbBar();
        localBreadcrumb.setActive(true);
        localBreadcrumb.setOnAddMapping(event -> AddMappingDialog.showMapLocalDialog(localBreadcrumb.getLocation()));
        getChildren().add(localBreadcrumb);
        setLocalCrumbActionHandler();
        final Collection<String> accounts = PathBundle.getAccounts();
        for (String account : accounts) {
            final RemoteFileBreadCrumbBar remoteBreadcrumb = new RemoteFileBreadCrumbBar(account);
            addRemoteCrumbEventHandler(remoteBreadcrumb);
            remoteBreadcrumb.setOnAddMapping(event -> AddMappingDialog.showMapRemoteDialog( remoteBreadcrumb.getAccount(), remoteBreadcrumb.getLocation()));
            setOnRemoveRemoteMapping(remoteBreadcrumb);
            remotebreadcrumbs.put(account, remoteBreadcrumb);
        }
        localBreadcrumb.activeProperty().bind(remote.not());
    }

    private void setOnRemoveRemoteMapping(RemoteFileBreadCrumbBar remoteBreadcrumb) {
        remoteBreadcrumb.setOnRemoveMapping(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println(remoteBreadcrumb.getLocation());
            }
        });
    }

    private void addRemoteCrumbEventHandler(RemoteFileBreadCrumbBar triggeredCrumbBar) {
        triggeredCrumbBar.setOnCrumbAction(event -> {
            remote.set(true);
            setAllRemoteCrumbsInactive();
            triggeredCrumbBar.setActive(true);
            if (remoteEventHandler != null) {
                remoteEventHandler.handle(event);
            }
        });
    }

    public Path getNewRemotePath(final BreadCrumbBar.BreadCrumbActionEvent<String> event, String account) {
        String path = "";
        TreeItem<String> selectedCrumb = event.getSelectedCrumb();
        while (selectedCrumb != null) {
            final String folder = selectedCrumb.getValue();
            if (folder.equals(account) && selectedCrumb.getParent() != null) {
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
        final Map<String, String> remoteFolders = PathBundle.getAllFolders(path.toString());
        getChildren().clear();
        if (path.toFile().exists()) {
            getChildren().add(localBreadcrumb);
        }
        for (Map.Entry<String, String> entry : remoteFolders.entrySet()) {
            final String account = entry.getKey();
            final RemoteFileBreadCrumbBar remoteBreadcrumb = remotebreadcrumbs.get(account);
            final String folderPath = entry.getValue();
            setRemoteBreadCrumb(folderPath, account, remoteBreadcrumb);
            if(remoteBreadcrumb.isActive()) {
                getChildren().add(0,remoteBreadcrumb);
            } else {
                getChildren().add(remoteBreadcrumb);
            }
            remoteBreadcrumb.setCanAddMapping(false);
        }
    }

    public void setRemoteBreadCrumbs(String account, Path path) {
        final RemoteFileBreadCrumbBar activeAccountCrumb = remotebreadcrumbs.get(account);
        activeAccountCrumb.setActive(true);
        final String crumbPath = path == null || Paths.get(account).equals(path) ? "" : path.toString();
        final String localFolder = PathBundle.getLocalFolder(account, crumbPath);
        if(isMapped(localFolder)) {
            setBreadCrumbs(Paths.get(localFolder));
        } else {
            getChildren().clear();
            activeAccountCrumb.setCanAddMapping(true);
            setRemoteBreadCrumb(crumbPath, account, activeAccountCrumb);
            getChildren().add(activeAccountCrumb);
        }
    }

    public void setCloudBreadCrumbs(String account) {
        getChildren().clear();
        final Collection<RemoteFileBreadCrumbBar> remoteCrumbBars = remotebreadcrumbs.values();
        for (RemoteFileBreadCrumbBar activeAccountCrumb : remoteCrumbBars) {
            activeAccountCrumb.setCanAddMapping(true);
            setRemoteBreadCrumb("", account, activeAccountCrumb);
            getChildren().add(activeAccountCrumb);
        }
    }

    private boolean isMapped(String localFolder) {
        return localFolder != null && !localFolder.isEmpty();
    }

    private void setBreadCrumb(String path, FileBreadCrumbBar breadcrumb) {
        TreeItem<String> model = BreadCrumbBar.buildTreeModel(path.split(SEPARATOR_PATTERN));
        if (model != null) {
            breadcrumb.setSelectedCrumb(model);
        }
    }

    private void setRemoteBreadCrumb(final String path, final String account, final RemoteFileBreadCrumbBar breadcrumb) {
        String breadcrumbPath = account + path;
        breadcrumbPath = SLASH_SEPARATOR.matcher(breadcrumbPath).replaceAll("\\\\");
        final TreeItem<String> model = BreadCrumbBar.buildTreeModel(breadcrumbPath.split(SEPARATOR_PATTERN));
        breadcrumb.setSelectedCrumb(model);
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
