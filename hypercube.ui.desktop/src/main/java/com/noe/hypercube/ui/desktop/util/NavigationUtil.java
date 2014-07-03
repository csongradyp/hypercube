package com.noe.hypercube.ui.desktop.util;

import com.noe.hypercube.ui.desktop.domain.File;
import com.noe.hypercube.ui.desktop.domain.LocalFile;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import org.controlsfx.control.BreadCrumbBar;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

public class NavigationUtil {

    public static String SEPARATOR = System.getProperty("file.separator");
    private static final String SEPARATOR_PATTERN = Pattern.quote(SEPARATOR);

    public static void navigateTo(TableView<File> table, BreadCrumbBar<String> breadCrumb, File directory) {
        if (directory.isStepBack()) {
            stepBack(table, breadCrumb, directory.getPath());
        } else {
            setData(table, breadCrumb, directory.getPath());
        }
    }

    public static void stepBack(TableView<File> table, BreadCrumbBar<String> breadCrumb, Path destination) {
        setData(table, breadCrumb, destination);
    }

    private static void setData(TableView<File> table, BreadCrumbBar<String> breadCrumb, Path directory) {
        if (directory != null && directory.toFile().isDirectory()) {
            boolean success = setTableData(table, directory);
            if (success) {
                setBreadCrumb(breadCrumb, directory);
                table.getSelectionModel().selectFirst();
            }
        }
    }

    private static void setBreadCrumb(BreadCrumbBar<String> breadCrumb, Path selectedItem) {
        TreeItem<String> model = BreadCrumbBar.buildTreeModel(selectedItem.toString().split(SEPARATOR_PATTERN));
        breadCrumb.setSelectedCrumb(model);
    }

    private static boolean setTableData(TableView<File> tableView, Path dir) {
        java.io.File[] list = dir.toFile().listFiles();
        Collection<File> files = new ArrayList<>(100);
        Collection<File> dirs = new ArrayList<>(100);
        if (dir.toFile().getParentFile() != null) {
            java.io.File parentFile = dir.toFile().getParentFile();
            File parent = new LocalFile(parentFile);
            parent.setStepBack(true);
            dirs.add(parent);
        }
        for (java.io.File file : list) {
            if (!file.isHidden() && Files.isReadable(file.toPath())) {
                if (file.isDirectory()) {
                    dirs.add(new LocalFile(file));
                } else {
                    files.add(new LocalFile(file));
                }
            }
        }
        dirs.addAll(files);
        ObservableList<File> data = FXCollections.observableArrayList(dirs);
        tableView.setItems(data);
        return !data.isEmpty();
    }
}
