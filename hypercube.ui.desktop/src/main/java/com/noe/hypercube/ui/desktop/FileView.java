package com.noe.hypercube.ui.desktop;

import com.noe.hypercube.observer.local.storage.LocalStorageObserver;
import com.noe.hypercube.ui.desktop.domain.File;
import com.noe.hypercube.ui.desktop.domain.IFile;
import com.noe.hypercube.ui.desktop.domain.LocalFile;
import com.noe.hypercube.ui.desktop.event.BreadCrumbEventHandler;
import com.noe.hypercube.ui.desktop.event.LocalStorageMouseEventHandler;
import com.noe.hypercube.ui.desktop.event.FileViewKeyEventHandler;
import com.noe.hypercube.ui.desktop.event.FileViewMouseEventHandler;
import com.noe.hypercube.ui.desktop.factory.FormattedTableCellFactory;
import com.noe.hypercube.ui.desktop.factory.StorageButtonFactory;
import com.noe.hypercube.ui.desktop.util.FileSizeCalculator;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.apache.commons.io.FilenameUtils;
import org.controlsfx.control.BreadCrumbBar;
import org.controlsfx.control.SegmentedButton;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;


public class FileView extends VBox implements Initializable {

    @FXML
    private TableView<File> table;
    @FXML
    private TableColumn<File, String> extColumn;
    @FXML
    private TableColumn<File, String> fileNameColumn;
    @FXML
    private TableColumn<File, String> fileSizeColumn;
    @FXML
    private TableColumn<File, String> dateColumnRight;

    @FXML
    private BreadCrumbBar<String> breadcrumb;
    @FXML
    private SegmentedButton localDrives;
    @FXML
    private SegmentedButton removableDrives;
    @FXML
    private SegmentedButton remoteDrives;

    private LocalStorageObserver storageObserver;

    private boolean selected;

    public FileView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("fileView.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupLocalDrives();
        setupRemoteDrives();
        setupFileTableView();
        breadcrumb.setOnCrumbAction(new BreadCrumbEventHandler(table));
//        observeRemovableDrives();
    }

    private void setupRemoteDrives() {
        ToggleButton addButton = new ToggleButton("+");
        addButton.setTooltip(new Tooltip("Add new remote drive"));
        remoteDrives.getButtons().add(addButton);
        addButton.setOnMouseClicked(event -> {
            remoteDrives.getButtons().add(0, new ToggleButton("New"));
            addButton.setSelected(false);
        });
    }

    private void setupFileTableView() {
        FileViewKeyEventHandler keyEventHandlerRight = new FileViewKeyEventHandler(table, breadcrumb);
        keyEventHandlerRight.init(new LocalFile( "c:/" ));
        table.setOnKeyPressed(keyEventHandlerRight);
        table.setOnMouseClicked(new FileViewMouseEventHandler(breadcrumb));
        Platform.runLater(() -> {
            if (isSelected()) {
                table.requestFocus();
            }
        });

        fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("Name"));

        fileSizeColumn.setCellFactory(new FormattedTableCellFactory<>(TextAlignment.RIGHT));
        fileSizeColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper(isStepBack(param) ? "" : FileSizeCalculator.calculate(param.getValue())));

        extColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper(!param.getValue().isDirectory() ? FilenameUtils.getExtension(param.getValue().getName()) : ""));

        dateColumnRight.setCellValueFactory(param -> new ReadOnlyObjectWrapper(isStepBack(param) ? "" : new Date(param.getValue().lastModified()).toString()));
    }

    private boolean isStepBack(TableColumn.CellDataFeatures<File, String> param) {
        return param.getValue().isStepBack();
    }

    private void setupLocalDrives() {
        List<ToggleButton> drives = new ArrayList<>(5);
        Iterable<Path> rootDirectories = FileSystems.getDefault().getRootDirectories();
        for (Path root : rootDirectories) {
            ToggleButton button = StorageButtonFactory.create(root, new LocalStorageMouseEventHandler(table, breadcrumb));
            drives.add(button);
        }
        localDrives.getButtons().addAll(drives);
        localDrives.getButtons().get(0).setSelected(true);
        localDrives.setOnMouseClicked(new LocalStorageMouseEventHandler(table, breadcrumb));
    }

//    private void observeRemovableDrives() {
//        storageObserver = new LocalStorageObserver();
//        storageObserver.onStorageAttachDetection(newRoot -> {
//            ToggleButton driveButton = StorageButtonFactory.create(newRoot, new LocalStorageMouseEventHandler(table, breadcrumb));
//            Platform.runLater(() -> removableDrives.getButtons().add(driveButton));
//        });
//        storageObserver.onStorageDetachDetection(storage -> {
//            ObservableList<ToggleButton> buttons = removableDrives.getButtons();
//            for (ToggleButton button : buttons) {
//                if (button.getText().contains(storage.toString())) {
//                    Platform.runLater(() -> buttons.remove(button));
//                }
//            }
//        });
//        storageObserver.start();
//    }

    public boolean isSelected() {
        return selected;
    }

    @FXML
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Path getCurrentPath() {
        String current = breadcrumb.getSelectedCrumb().getValue();
        return Paths.get(current);
    }

    public Path getLocation() {
        IFile IFile = table.getSelectionModel().getSelectedItem();
        return IFile.getPath();
    }

    public Path getActiveDirectory() {
        IFile IFile = table.getSelectionModel().getSelectedItem();
        return IFile.getPath().getParent();
    }
}
