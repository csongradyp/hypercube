package com.noe.hypercube.ui;

import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import com.noe.hypercube.ui.bundle.ImageBundle;
import com.noe.hypercube.ui.bundle.PathBundle;
import com.noe.hypercube.ui.domain.file.IFile;
import com.noe.hypercube.ui.domain.file.LocalFile;
import com.noe.hypercube.ui.domain.file.RemoteFile;
import com.noe.hypercube.ui.domain.file.StepBackFile;
import com.noe.hypercube.ui.elements.FileListComparator;
import com.noe.hypercube.ui.factory.FileCellFactory;
import com.noe.hypercube.ui.factory.IconFactory;
import com.noe.hypercube.ui.util.FileSizeCalculator;
import com.noe.hypercube.util.DateUtil;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
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
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;
import org.apache.commons.io.FilenameUtils;

public class FileTableView extends TableView<IFile> implements Initializable {

    @FXML
    private TableColumn<IFile, IFile> cloudColumn;
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

        cloudColumn.setGraphic(AwesomeDude.createIconLabel(AwesomeIcon.CLOUD, "15"));
        cloudColumn.setCellValueFactory(file -> new ReadOnlyObjectWrapper<>(file.getValue()));
        cloudColumn.setCellFactory(new FileCellFactory(file -> {
            HBox content = new HBox(2.0d);
            if (!file.isStepBack() && !file.isLocal()) {
                final Label accountMarkLabel = createAccountMarkLabel(file.getOrigin());
                accountMarkLabel.getStyleClass().add("account-icon");
                content.getChildren().add(accountMarkLabel);
            }
            final Collection<String> sharedWith = file.sharedWith();
            for (final String account : sharedWith) {
                final Label sharedAccountMarkLabel = createAccountMarkLabel(account);
                sharedAccountMarkLabel.getStyleClass().add("account-icon");
                content.getChildren().add(sharedAccountMarkLabel);
            }
            return content;
        }));

        fileNameColumn.setCellValueFactory(file -> new ReadOnlyObjectWrapper<>(file.getValue()));
        fileNameColumn.setComparator(new FileListComparator());
        fileNameColumn.setSortable(true);
        fileNameColumn.setCellFactory(new FileCellFactory(file -> {
            Label iconLabel = IconFactory.getFileIcon(file);
            iconLabel.getStyleClass().removeAll("table-row-marked", "table-row-shared");
            if (file.isMarked()) {
                iconLabel.getStyleClass().add("table-row-marked");
            }
            if (file.isShared()) {
                iconLabel.getStyleClass().add("table-row-shared");
            }
            return iconLabel;
        }));

        fileSizeColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        fileSizeColumn.setCellFactory(new FileCellFactory(TextAlignment.RIGHT, file -> file.isStepBack() ? "" : FileSizeCalculator.calculate(file)));

        extColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        extColumn.setCellFactory(new FileCellFactory(TextAlignment.CENTER, file -> file.isDirectory() ? "" : FilenameUtils.getExtension(file.getName())));

        dateColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        dateColumn.setCellFactory(new FileCellFactory(TextAlignment.RIGHT, file -> file.lastModified() == 0L ? "" : DateUtil.format(file.lastModified())));

        setOnDragDetected(event -> {
            if (event.isSecondaryButtonDown()) {
                startFullDrag();
            }
            event.consume();
        });
    }

    private Label createAccountMarkLabel(final String account) {
        final Label accountMark = new Label();
        accountMark.setId(account);
        final ImageView icon = ImageBundle.getAccountImageView(account);
        accountMark.setGraphic(icon);
        accountMark.setTooltip(new Tooltip(account));
        return accountMark;
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
        final Collection<IFile> files = new ArrayList<>(100);
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
                    if (!accounts.isEmpty()) {
                        localFolder.share(accounts);
                    }
                    dirs.add(localFolder);
                } else if (Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
                    files.add(new LocalFile(file));
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        dirs.addAll(files);
        setItems(FXCollections.observableArrayList(dirs));
        getSortOrder().add(fileNameColumn);
        selectIfSteppedBack(previousFolder);
    }

    private IFile getPreviousFolder(final Path previousDirectory) {
        if (previousDirectory != null) {
            return new StepBackFile(previousDirectory.toFile());
        }
        return null;
    }

    private void selectIfSteppedBack(IFile previousFolder) {
        final ObservableList<IFile> items = getItems();
        if (previousFolder != null && items.contains(previousFolder)) {
            scrollTo(previousFolder);
            getSelectionModel().select(previousFolder);
        } else {
            scrollTo(0);
            getSelectionModel().selectFirst();
        }
    }

    public void setRemoteFileList(final String account, final Path previousParentFolder, final Path parentFolder, final List<ServerEntry> list) {
        final Collection<IFile> files = new ArrayList<>(100);
        final Collection<IFile> dirs = new ArrayList<>(100);
        final Path currentLocation = getLocation();
        IFile previousFolder = null;
        if (currentLocation == null) {
            previousFolder = new StepBackFile(account, Paths.get("/"));
        } else if (currentLocation.startsWith("/")) {
            previousFolder = new StepBackFile(account, previousParentFolder);
        }
        if (list.isEmpty()) {
            final IFile stepBack = createStepBackFile(currentLocation);
            dirs.add(stepBack);
        } else {
            if (!parentFolder.toString().isEmpty()) {
                final IFile stepBack = new StepBackFile(account, Paths.get(account, parentFolder.getParent() == null ? "/" : parentFolder.getParent().toString()));
                dirs.add(stepBack);
            }
            for (ServerEntry file : list) {
                final Path path = Paths.get(file.getAccount(), file.getPath().toString());
                final RemoteFile remoteFile = new RemoteFile(file.getAccount(), path, 0, file.isFolder(), file.lastModified());
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
        getSortOrder().add(fileNameColumn);
        selectIfSteppedBack(previousFolder);
    }

    public void setCloudFileList(final Path parentFolder, final List<ServerEntry> list) {
        final Collection<IFile> files = new ArrayList<>(100);
        final Collection<IFile> dirs = new ArrayList<>(100);
        final IFile previousFolder = new LocalFile(getLocation());
        if (list.isEmpty()) {
            final IFile stepBack = createStepBackFile(getLocation());
            dirs.add(stepBack);
        } else {
            final IFile stepBack = createStepBackFile(parentFolder);
            if (stepBack != null) {
                dirs.add(stepBack);
            }
            for (ServerEntry file : list) {
                final RemoteFile remoteFile = new RemoteFile(file.getAccount(), Paths.get(file.getAccount(), file.getPath().toString()), 0, file.isFolder(), file.lastModified());
//                remoteFile.share(file.getAccount());
                if (getItems().contains(remoteFile)) {
                    final IFile iFile = getItems().filtered(file1 -> file1.equals(remoteFile)).get(0);
                    iFile.share(remoteFile.sharedWith());
                } else {
                    if (file.isFolder()) {
                        dirs.add(remoteFile);
                    } else {
                        files.add(remoteFile);
                    }
                }
            }
            dirs.addAll(files);
        }
        ObservableList<IFile> data = FXCollections.observableArrayList(dirs);
        getItems().addAll(data);
        getSortOrder().add(fileNameColumn);
        selectIfSteppedBack(previousFolder);
    }

    private IFile createStepBackFile(final Path dir) {
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

    public void setLocation(final Path location) {
        this.location.set(location);
    }
}
