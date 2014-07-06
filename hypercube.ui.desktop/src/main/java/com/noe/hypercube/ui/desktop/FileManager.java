package com.noe.hypercube.ui.desktop;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class FileManager extends VBox implements Initializable {

    @FXML
    private HBox doubleView;
    @FXML
    private FileView leftFileView;
    @FXML
    private FileView rightFileView;

    @FXML
    private Button copy;
    @FXML
    private Button edit;
    @FXML
    private Button delete;
    @FXML
    private Button move;
    @FXML
    private Button exit;
    @FXML
    private Button newFolder;
    @FXML
    private Button upload;
    @FXML
    private Button download;

    public FileManager() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("fileManager.fxml"));
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
        leftFileView.setLocation(Paths.get("C:"));
        rightFileView.setLocation(Paths.get("C:"));
    }

    @FXML
    public void onCopy(ActionEvent e) {
        FileView activeFileView = getActiveFileView();
        FileView inactiveFileView = getInactiveFileView();
//        FileUtils.copyFile(  );
        Path location = activeFileView.getLocation();
        System.out.println(location + " to " + inactiveFileView.getActiveDirectory());
    }

    @FXML
    public void onMove(ActionEvent e) {
        System.out.println(leftFileView.getLocation());
    }

    @FXML
    public void onNewFolder(ActionEvent e) {
        System.out.println(leftFileView.getLocation());
    }

    @FXML
    public void onDelete(ActionEvent e) {
        System.out.println(leftFileView.getLocation());
    }

    @FXML
    public void onEdit(ActionEvent e) {
        System.out.println(leftFileView.getLocation());
    }

    @FXML
    public void onDownload(ActionEvent e) {
        System.out.println(leftFileView.getLocation());
    }

    @FXML
    public void onUpload(ActionEvent e) {
        System.out.println(leftFileView.getLocation());
    }

    @FXML
    public void onExit() {
        Platform.exit();
    }

    private FileView getActiveFileView() {
        if (leftFileView.isSelected()) {
            return leftFileView;
        }
        return rightFileView;
    }

    private FileView getInactiveFileView() {
        if (!leftFileView.isSelected()) {
            return leftFileView;
        }
        return rightFileView;
    }
}
