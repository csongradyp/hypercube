package com.noe.hypercube.ui.desktop;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.controlsfx.control.BreadCrumbBar;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private BreadCrumbBar<String> breadcrumbLeft;
    @FXML
    private BreadCrumbBar<String> breadcrumbRight;

    @FXML
    private TableView<File> tableLeft;
    @FXML
    private TableColumn<File, String> filenameColumn;
    @FXML
    private TableColumn<File, String> filesizeColumn;
    @FXML
    private TableColumn<File, String> extColumn;
    @FXML
    private TableColumn<File, String> dateColumn;
    @FXML
    private TableColumn<File, String> filenameColumnRight;
    @FXML
    private TableColumn<File, String> filesizeColumnRight;
    @FXML
    private TableColumn<File, String> extColumnRight;
    @FXML
    private TableColumn<File, String> dateColumnRight;

    @FXML
    private TableView<File> tableRight;

    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        FileListKeyEventHandler rightHandler = new FileListKeyEventHandler(tableRight, breadcrumbRight);
//        rightHandler.init(new File("c:\\"));
//        tableRight.setOnKeyPressed(rightHandler);
//        FileListKeyEventHandler leftHandler = new FileListKeyEventHandler(tableLeft, breadcrumbLeft);
//        leftHandler.init(new File("d:\\"));
//        tableLeft.setOnKeyPressed(leftHandler);
//
//        filenameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
//        filesizeColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper(FileSizeCalculator.calculate(param.getValue())));
//        extColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper(param.getValue().isFile() ? FilenameUtils.getExtension(param.getValue().getName()) : ""));
//        dateColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper(new Date(param.getValue().lastModified()).toString()));
//
//        filenameColumnRight.setCellValueFactory(new PropertyValueFactory<>("name"));
//        filesizeColumnRight.setCellValueFactory(param -> new ReadOnlyObjectWrapper(FileSizeCalculator.calculate(param.getValue())));
//        extColumnRight.setCellValueFactory(param -> new ReadOnlyObjectWrapper(param.getValue().isFile() ? FilenameUtils.getExtension(param.getValue().getName()) : ""));
//        dateColumnRight.setCellValueFactory(param -> new ReadOnlyObjectWrapper(new Date(param.getValue().lastModified()).toString()));
//
//        Platform.runLater(tableLeft::requestFocus);
    }
}
