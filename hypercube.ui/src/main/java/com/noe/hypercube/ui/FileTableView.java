package com.noe.hypercube.ui;

import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import com.noe.hypercube.ui.domain.IFile;
import com.noe.hypercube.ui.domain.LocalFile;
import com.noe.hypercube.ui.domain.RemoteFile;
import com.noe.hypercube.ui.factory.FileCellFactory;
import com.noe.hypercube.ui.factory.IconFactory;
import com.noe.hypercube.ui.util.DateUtil;
import com.noe.hypercube.ui.util.FileSizeCalculator;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
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
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.TextAlignment;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FileTableView extends TableView<IFile> implements Initializable {

    @FXML
    private TableColumn<IFile, IFile> sharedColumn;
    @FXML
    private TableColumn<IFile, IFile> fileNameColumn;
    @FXML
    private TableColumn<IFile, IFile> extColumn;
    @FXML
    private TableColumn<IFile, IFile> fileSizeColumn;
    @FXML
    private TableColumn<IFile, IFile> dateColumn;

    private final SimpleObjectProperty<Path> location = new SimpleObjectProperty<>();
    private final SimpleBooleanProperty active = new SimpleBooleanProperty(false);

    public FileTableView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("fileTableView.fxml"));
        fxmlLoader.setResources(ResourceBundle.getBundle("internationalization/messages", new Locale(ConfigurationBundle.getLanguage())));
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
        setEmptyTablePlaceholder(resources);
        focusedProperty().addListener((observable, oldValue, newValue) -> setActive(newValue));
        requestFocusIfActive();

        sharedColumn.setGraphic(AwesomeDude.createIconLabel(AwesomeIcon.CODE_FORK, "15"));

        fileNameColumn.setCellValueFactory(file -> new ReadOnlyObjectWrapper<>(file.getValue()));
        fileNameColumn.setCellFactory(new FileCellFactory(file -> {
            Label label = IconFactory.getFileIcon(file);
            if (file.isMarked()) {
                label.getStyleClass().add("table-row-marked");
                getStyleClass().add("table-row-marked");
            } else if (file.isShared()) {
                getStyleClass().add("table-row-special");
                label.getStyleClass().add("table-row-special");
            } else {
                label.getStyleClass().removeAll("table-row-marked", "table-row-special");
                getStyleClass().removeAll("table-row-marked", "table-row-special");
            }
            return label;
        }));

        fileSizeColumn.setCellFactory(new FileCellFactory(TextAlignment.RIGHT, file -> file.isStepBack() ? "" : FileSizeCalculator.calculate(file)));
        fileSizeColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));

        extColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        extColumn.setCellFactory(new FileCellFactory(TextAlignment.CENTER, file -> !file.isDirectory() ? FilenameUtils.getExtension(file.getName()) : ""));

        dateColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        dateColumn.setCellFactory(new FileCellFactory(TextAlignment.RIGHT, file -> DateUtil.format(file.lastModified())));
    }

    private void setEmptyTablePlaceholder(ResourceBundle resources) {
        final Label iconLabel = AwesomeDude.createIconLabel(AwesomeIcon.PUZZLE_PIECE, resources.getString("table.empty"), "100px", "12", ContentDisplay.TOP);
        iconLabel.getGraphic().setOpacity(0.3d);
        setPlaceholder(iconLabel);
    }

    private void requestFocusIfActive() {
        Platform.runLater(() -> {
            if (isActive()) {
                requestFocus();
            }
        });
    }

    public void setLocalFileList(Path dir) {
        java.io.File[] list = dir.toFile().listFiles();
        Collection<IFile> files = new ArrayList<>(100);
        Collection<IFile> dirs = new ArrayList<>(100);
        IFile stepBack = createStepBackFile(dir);
        if (stepBack != null) {
            dirs.add(stepBack);
        }
        if (list != null) {
            for (java.io.File file : list) {
                if (!file.isHidden() && Files.isReadable(file.toPath())) {
                    if (file.isDirectory()) {
                        dirs.add(new LocalFile(file));
                    } else {
                        files.add(new LocalFile(file));
                    }
                }
            }
        }
        dirs.addAll(files);
        ObservableList<IFile> data = FXCollections.observableArrayList(dirs);
        setItems(data);
        getSelectionModel().selectFirst();
    }

    public void setRemoteFileList(final Path parentFolder, final List<ServerEntry> list) {
        final Collection<IFile> files = new ArrayList<>(100);
        final Collection<IFile> dirs = new ArrayList<>(100);
        if (!list.isEmpty()) {
            final IFile stepBack = createStepBackFile(parentFolder);
            if (stepBack != null) {
                dirs.add(stepBack);
            }
            for (ServerEntry file : list) {
                final RemoteFile remoteFile = new RemoteFile(file.getPath(), 0, file.isFolder(), file.lastModified());
                if (file.isFolder()) {
                    dirs.add(remoteFile);
                } else {
                    files.add(remoteFile);
                }
            }
            getItems().clear();
            dirs.addAll(files);
        } else {
            final Path location1 = getLocation();
            final IFile stepBack = createStepBackFile(location1);
            dirs.add(stepBack);
        }
        ObservableList<IFile> data = FXCollections.observableArrayList(dirs);
        setItems(data);
        getSelectionModel().selectFirst();
    }

    private IFile createStepBackFile(Path dir) {
        if (dir != null && dir.toFile().getParentFile() != null) {
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
