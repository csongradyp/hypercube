package com.noe.hypercube.ui;

import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import com.noe.hypercube.ui.bundle.PathBundle;
import com.noe.hypercube.ui.domain.IFile;
import com.noe.hypercube.ui.domain.LocalFile;
import com.noe.hypercube.ui.domain.RemoteFile;
import com.noe.hypercube.ui.domain.StepBackFile;
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
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
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
    private final DirectoryStream.Filter<Path> localFileFilter = path -> Files.isReadable(path) && !path.toFile().isHidden();

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
        sharedColumn.setCellValueFactory(file -> new ReadOnlyObjectWrapper<>(file.getValue()));
        sharedColumn.setCellFactory(new FileCellFactory(file -> {
            HBox content = new HBox(2.0d);
            final Collection<String> sharedWith = file.sharedWith();
            for (final String account : sharedWith) {
                final Label accountMark = new Label();
                // TODO add icon
                AwesomeDude.setIcon(accountMark, AwesomeIcon.DROPBOX);
                accountMark.setTooltip(new Tooltip(account));
                content.getChildren().add(accountMark);
            }
            return content;
        }));

        fileNameColumn.setCellValueFactory(file -> new ReadOnlyObjectWrapper<>(file.getValue()));
        fileNameColumn.setCellFactory(new FileCellFactory(file -> {
            Label label = IconFactory.getFileIcon(file);
            label.getStyleClass().removeAll("table-row-marked", "table-row-shared");
            if (file.isMarked()) {
                label.getStyleClass().add("table-row-marked");
            }
            if (file.isShared()) {
                label.getStyleClass().add("table-row-shared");
            }
            return label;
        }));

        fileSizeColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        fileSizeColumn.setCellFactory(new FileCellFactory(TextAlignment.RIGHT, file -> file.isStepBack() ? "" : FileSizeCalculator.calculate(file)));

        extColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        extColumn.setCellFactory(new FileCellFactory(TextAlignment.CENTER, file -> file.isDirectory() ? "" : FilenameUtils.getExtension(file.getName())));

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

    public void setLocalFileList(final Path directory, final Path previousDirectory) {
        final Collection <IFile> files = new ArrayList<>(100);
        final Collection<IFile> dirs = new ArrayList<>(100);
        final IFile stepBack = createStepBackFile(directory);
        final IFile previousFolder = getPreviousFolder(previousDirectory);
        if (stepBack != null) {
            dirs.add(stepBack);
        }
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory, localFileFilter)) {
            for (Path file : directoryStream) {
                if (Files.isDirectory(file, LinkOption.NOFOLLOW_LINKS)) {
                    final LocalFile localFolder = new LocalFile(file);
                    final Set<String> accounts = PathBundle.getAccounts(localFolder);
                    if(!accounts.isEmpty()) {
                        localFolder.sharedWith(accounts);
                    }
                    dirs.add(localFolder);
                } else if(Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)){
                    files.add(new LocalFile(file));
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        dirs.addAll(files);
        setItems(FXCollections.observableArrayList(dirs));
        selectIfSteppedBack(previousFolder);
    }

    private IFile getPreviousFolder(Path previousDirectory) {
        if(previousDirectory != null) {
            return new StepBackFile(previousDirectory);
        }
        return null;
    }

    private void selectIfSteppedBack(IFile previousFolder) {
        if(previousFolder != null && getItems().contains(previousFolder)) {
            scrollTo(previousFolder);
            getSelectionModel().select(previousFolder);
        } else {
            scrollTo(0);
            getSelectionModel().selectFirst();
        }
    }

    public void setRemoteFileList(final Path parentFolder, final List<ServerEntry> list) {
        final Collection<IFile> files = new ArrayList<>(100);
        final Collection<IFile> dirs = new ArrayList<>(100);
        final Path currentLocation = getLocation();
        IFile previousFolder = null;
        if(currentLocation.startsWith("/")) {
            previousFolder = new StepBackFile(currentLocation);
        }
        if (list.isEmpty()) {
            final IFile stepBack = createStepBackFile(currentLocation);
            dirs.add(stepBack);
        } else {
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
        }
        ObservableList<IFile> data = FXCollections.observableArrayList(dirs);
        setItems(data);
        selectIfSteppedBack(previousFolder);
    }

    private IFile createStepBackFile(Path dir) {
        if (dir != null && dir.toFile().getParentFile() != null) {
            java.io.File parentFile = dir.toFile().getParentFile();
            return new StepBackFile(parentFile);
        }
        return null;
    }

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
