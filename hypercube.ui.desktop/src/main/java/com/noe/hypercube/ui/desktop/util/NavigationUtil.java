package com.noe.hypercube.ui.desktop.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import org.controlsfx.control.BreadCrumbBar;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class NavigationUtil {

    public static final String TO_PARENT_PLACEHOLDER = "[ . . ]";
    public static String SEPARATOR = System.getProperty("file.separator");
    private static final String SEPARATOR_PATTERN = Pattern.quote(SEPARATOR);

    public static void navigateTo(TableView<File> table, BreadCrumbBar<String> breadCrumb, File directory) {
        long start = System.currentTimeMillis();
        if (directory.getName().equals(TO_PARENT_PLACEHOLDER)) {
            stepBack(table, breadCrumb, directory);
        } else {
            setData(table, breadCrumb, directory);
        }
        System.out.println(System.currentTimeMillis() - start + "ms - navigateTo " + directory.toPath());
    }

    public static void stepBack(TableView<File> table, BreadCrumbBar<String> breadCrumb, File currentDirectory) {
        File parentDir = currentDirectory.getParentFile();
        setData(table, breadCrumb, parentDir);
    }

    private static void setData(TableView<File> table, BreadCrumbBar<String> breadCrumb, File directory) {
        if (directory != null && directory.isDirectory()) {
            boolean success = setTableData(table, directory);
            if (success) {
                setBreadCrumb(breadCrumb, directory);
                table.getSelectionModel().selectFirst();
            }
        }
    }

    private static void setBreadCrumb(BreadCrumbBar<String> breadCrumb, File selectedItem) {
        TreeItem<String> model = BreadCrumbBar.buildTreeModel(selectedItem.getPath().split(SEPARATOR_PATTERN));
        breadCrumb.setSelectedCrumb(model);
    }

    private static boolean setTableData(TableView<File> tableView, File dir) {
        long start = System.currentTimeMillis();
        File[] list = dir.listFiles();
        List<File> files = new ArrayList<>(100);
        List<File> dirs = new ArrayList<>(100);
        if(dir.getParentFile() != null) {
            File parentFile = dir.getParentFile();
            File parent = new File(parentFile, TO_PARENT_PLACEHOLDER);
            dirs.add(parent);
        }
        for(File file : list) {
            if (!file.isHidden() && Files.isReadable(file.toPath())) {
                if (file.isDirectory()) {
                    dirs.add(file);
                } else {
                    files.add(file);
                }
            }
        }
        dirs.addAll(files);
        ObservableList<File> data = FXCollections.observableArrayList(dirs);
        tableView.setItems(data);
        System.out.println(System.currentTimeMillis() - start + "ms - setTableData " + dir.toPath());
        return !data.isEmpty();
    }
}
