package com.noe.hypercube.ui;

import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.ui.dialog.FileProgressDialog;
import com.noe.hypercube.ui.domain.IFile;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialogs;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
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
        leftFileView.initStartLocation();
        rightFileView.initStartLocation();
        EventBus.subscribeToStorageEvent(leftFileView);
        EventBus.subscribeToStorageEvent(rightFileView);
    }

    @FXML
    public void onCopy(ActionEvent e) {
        FileView activeFileView = getActiveFileView();
        FileView inactiveFileView = getInactiveFileView();

        Collection<IFile> markedFiles = activeFileView.getMarkedFiles();
        Action action = Dialogs.create().title("Copy file(s)").message(markedFiles.toString()).showConfirm();
        if ("Yes".equals(action.textProperty().getValue())) {
            FileProgressDialog test = new FileProgressDialog(this, "Copy file(s)", activeFileView.getLocation(), inactiveFileView.getLocation());
            test.show();
            System.out.println(markedFiles);
            for (IFile markedFile : markedFiles) {
                Path destination = Paths.get(inactiveFileView.getLocation().toString(), markedFile.getName());
                System.out.println(markedFile + " to " + destination);
            }
        }
        for (IFile markedFile : markedFiles) {
            markedFile.setMarked(false);
        }


//        try {
//            ProgressAwareInputStream progressAwareInputStream = new ProgressAwareInputStream( new FileInputStream( source ), source.length(), null );
//            new Thread( () -> { try {
//                Files.copy( progressAwareInputStream, destination, StandardCopyOption.REPLACE_EXISTING );
//            }
//            catch ( IOException e1 ) {
//                e1.printStackTrace();
//            }
//            } ).start();
//            DialogFactory.createProgressDialog( "Copy file(s)", progressAwareInputStream ).show();
//
//        } catch ( IOException e2 ) {
//            e2.printStackTrace();
//        }


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
        if (leftFileView.isActive()) {
            return leftFileView;
        }
        return rightFileView;
    }

    private FileView getInactiveFileView() {
        if (!leftFileView.isActive()) {
            return leftFileView;
        }
        return rightFileView;
    }
}
