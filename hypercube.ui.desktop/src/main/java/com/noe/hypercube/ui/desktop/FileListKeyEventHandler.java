package com.noe.hypercube.ui.desktop;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.controlsfx.control.BreadCrumbBar;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class FileListKeyEventHandler implements EventHandler<KeyEvent> {

    private final TableView<File> tableView;
    private final BreadCrumbBar<String> breadCrumb;
    private final Pattern pattern;

    public FileListKeyEventHandler(TableView<File> tableView, BreadCrumbBar<String> breadCrumb) {
        this.tableView = tableView;
        this.breadCrumb = breadCrumb;
        pattern = Pattern.compile(Pattern.quote(System.getProperty("file.separator")));
    }

    public void init(File startLocation) {
        setBreadCrumb(startLocation);
        setTableData(startLocation);
    }

    @Override
    public void handle(KeyEvent event) {
        TableView source = (TableView) event.getSource();
        File selectedFile = (File) source.getSelectionModel().getSelectedItem();

        if (event.getCode() == KeyCode.BACK_SPACE) {
            File parentDirectory = selectedFile.getParentFile().getParentFile();
            navigateTo(parentDirectory);
        } else if (event.getCode() == KeyCode.ENTER) {
            navigateTo(selectedFile);
        } else if (event.getCode() == KeyCode.SPACE) {
            TablePosition focusedCell = source.getFocusModel().getFocusedCell();
            //                filesizeColumn.getCellValueFactory().call( new TableColumn.CellDataFeatures(source, filesizeColumn, ) );
        }
    }

    private void navigateTo(File directory) {
        if (directory != null && directory.isDirectory()) {
            boolean success = setTableData(directory);
            if (success) {
                setBreadCrumb(directory);
                tableView.getSelectionModel().selectFirst();
            }
        }
    }

    private void setBreadCrumb(File selectedItem) {
        String[] pathDirectories = pattern.split(selectedItem.getPath());
        TreeItem<String> model = BreadCrumbBar.buildTreeModel(pathDirectories);
        breadCrumb.setSelectedCrumb(model);
    }

    private boolean setTableData(File dir) {
        File[] list = dir.listFiles();
        List<File> files = new ArrayList<>(100);
        List<File> dirs = new ArrayList<>(100);
        for (File file : list) {
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
        tableView.getItems().clear();
        tableView.getItems().addAll(data);
        return !data.isEmpty();
    }
}


