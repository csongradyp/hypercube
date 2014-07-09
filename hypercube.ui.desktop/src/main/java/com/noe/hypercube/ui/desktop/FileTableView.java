package com.noe.hypercube.ui.desktop;

import com.noe.hypercube.ui.desktop.domain.IFile;
import com.noe.hypercube.ui.desktop.domain.LocalFile;
import com.noe.hypercube.ui.desktop.factory.FileCellFactory;
import com.noe.hypercube.ui.desktop.factory.IconFactory;
import com.noe.hypercube.ui.desktop.util.FileSizeCalculator;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;
import org.apache.commons.io.FilenameUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;

public class FileTableView extends TableView<IFile> implements Initializable {

    @FXML
    private TableColumn<IFile, IFile> extColumn;
    @FXML
    private TableColumn<IFile, IFile> fileNameColumn;
    @FXML
    private TableColumn<IFile, IFile> fileSizeColumn;
    @FXML
    private TableColumn<IFile, IFile> dateColumn;

    private final SimpleObjectProperty<Path> location = new SimpleObjectProperty<>();
    private final SimpleBooleanProperty active = new SimpleBooleanProperty(false);

    public FileTableView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("fileTableView.fxml"));
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
        Platform.runLater(() -> {
            if (isActive()) {
                requestFocus();
            }
        });

        fileNameColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        fileNameColumn.setCellFactory(new FileCellFactory(item -> {
            HBox box = new HBox(10);
            ImageView imageview = new ImageView(IconFactory.getFileIcon(item));
            Label label = new Label(item.getName());
            box.getChildren().addAll(imageview, label);
            if (item.isSelected()) {
                label.getStyleClass().add("table-row-marked");
                getStyleClass().add("table-row-marked");
            } else {
                label.getStyleClass().remove("table-row-marked");
                getStyleClass().remove("table-row-marked");
            }

            return box;
        }));

        fileSizeColumn.setCellFactory(new FileCellFactory(TextAlignment.RIGHT, item -> item.isStepBack() ? "" : FileSizeCalculator.calculate(item)));
        fileSizeColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));

        extColumn.setCellFactory(new FileCellFactory(TextAlignment.CENTER, param -> !param.isDirectory() ? FilenameUtils.getExtension(param.getName()) : ""));
        extColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy.MM.dd  HH:mm:ss");
        dateColumn.setCellFactory(new FileCellFactory(TextAlignment.RIGHT, item -> dateTimeFormatter.print(item.lastModified())));
        dateColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
    }

    private boolean isStepBack(IFile param) {
        return param.isStepBack();
    }

    public void updateLocation(Path dir) {
        java.io.File[] list = dir.toFile().listFiles();
        Collection<IFile> files = new ArrayList<>(100);
        Collection<IFile> dirs = new ArrayList<>(100);
        IFile stepBack = createStepBackFile(dir);
        if (stepBack != null) {
            dirs.add(stepBack);
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
        ObservableList<IFile> data = FXCollections.observableArrayList(dirs);
        setItems(data);
        getSelectionModel().selectFirst();
    }

    private IFile createStepBackFile(Path dir) {
        if (dir.toFile().getParentFile() != null) {
            java.io.File parentFile = dir.toFile().getParentFile();
            return new LocalFile(parentFile, true);
        }
        return null;
    }

    @FXML
    public void setActive(boolean active) {
        this.active.set(active);
    }

    public boolean isActive() {
        return active.get();
    }

    public BooleanProperty getActiveProperty() {
        return active;
    }

    public SimpleObjectProperty<Path> getLocationProperty() {
        return location;
    }

    public Path getLocation() {
        return location.get();
    }

    public void setLocation(Path location) {
        this.location.set(location);
    }
}
