package com.noe.hypercube.ui;

import com.noe.hypercube.ui.bundle.PathBundle;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class MultiBreadCrumbBar extends VBox implements Initializable {

    private static final String SEPARATOR = System.getProperty("file.separator");
    private static final String SEPARATOR_PATTERN = Pattern.quote(SEPARATOR);
    private static final Pattern COMPILE = Pattern.compile("/");

    private PathBundle pathBundle;
    private BreadCrumbBar<String> localBreadcrumb;
    private Map<String, BreadCrumbBar<String>> breadCrumbBars;

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
        breadCrumbBars = new HashMap<>();
        localBreadcrumb = new BreadCrumbBar<>();
        final Collection<String> accounts = pathBundle.getAccounts();
        for (String account : accounts) {
            breadCrumbBars.put(account, new BreadCrumbBar<>());
        }
        disableBreadcrumbFocusTraversal(localBreadcrumb);
        breadCrumbBars.values().forEach(this::disableBreadcrumbFocusTraversal);
        getChildren().add(localBreadcrumb);

    }

    public void setBreadCrumbs(Path path) {
        TreeItem<String> model = BreadCrumbBar.buildTreeModel(path.toString().split(SEPARATOR_PATTERN));
        localBreadcrumb.setSelectedCrumb(model);
        setBreadCrumb(path, localBreadcrumb);
        final Map<String, String> remoteFolders = pathBundle.getAllRemoteFolders(path.toString());
        getChildren().clear();
        if (path.toFile().exists()) {
            getChildren().add(localBreadcrumb);
        }
        for (Map.Entry<String, String> entry : remoteFolders.entrySet()) {
            final BreadCrumbBar<String> remoteBreadcrumb = breadCrumbBars.get(entry.getKey());
            setRemoteBreadCrumb(entry.getValue(), entry.getKey(), remoteBreadcrumb);
            getChildren().add(remoteBreadcrumb);
        }
    }

    private void setBreadCrumb(Path path, BreadCrumbBar<String> breadcrumb) {
        TreeItem<String> model = BreadCrumbBar.buildTreeModel(path.toString().split(SEPARATOR_PATTERN));
        breadcrumb.setSelectedCrumb(model);
    }

    private void setRemoteBreadCrumb(final String path, final String account, final BreadCrumbBar<String> breadcrumb) {
        String breadcrumbPath = account + '/' + path;
        breadcrumbPath = COMPILE.matcher(breadcrumbPath).replaceAll("\\\\");
        TreeItem<String> model = BreadCrumbBar.buildTreeModel(breadcrumbPath.split(SEPARATOR_PATTERN));
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
        localBreadcrumb.setOnCrumbAction(eventHandler);
    }

    public void setOnRemoteCrumbAction(EventHandler<BreadCrumbBar.BreadCrumbActionEvent<String>> eventHandler) {
        for (Map.Entry<String, BreadCrumbBar<String>> entry : breadCrumbBars.entrySet()) {
            entry.getValue().setOnCrumbAction(eventHandler);
        }
    }

}
