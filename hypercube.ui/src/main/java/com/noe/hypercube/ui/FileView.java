package com.noe.hypercube.ui;

import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.EventHandler;
import com.noe.hypercube.event.domain.request.FileListRequest;
import com.noe.hypercube.event.domain.response.FileListResponse;
import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import com.noe.hypercube.ui.bundle.ImageBundle;
import com.noe.hypercube.ui.domain.file.IFile;
import com.noe.hypercube.ui.domain.file.LocalFile;
import com.noe.hypercube.ui.elements.AccountSegmentedButton;
import com.noe.hypercube.ui.elements.LocalDriveSegmentedButton;
import com.noe.hypercube.ui.util.StyleUtil;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.css.Styleable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import net.engio.mbassy.listener.Handler;
import org.controlsfx.control.BreadCrumbBar;
import org.controlsfx.control.SegmentedButton;

import static com.noe.hypercube.ui.util.PathConverterUtil.getEventPath;
import static javafx.scene.input.KeyCombination.ModifierValue.DOWN;
import static javafx.scene.input.KeyCombination.ModifierValue.UP;

public class FileView extends VBox implements Initializable, EventHandler<FileListResponse> {

    private final KeyCombination enter = new KeyCodeCombination(KeyCode.ENTER);
    private final KeyCombination backSpace = new KeyCodeCombination(KeyCode.BACK_SPACE);
    private final KeyCombination space = new KeyCodeCombination(KeyCode.SPACE);
    private final KeyCombination shiftDown = new KeyCodeCombination(KeyCode.DOWN, DOWN, UP, UP, UP, UP);
    private final KeyCombination shiftUp = new KeyCodeCombination(KeyCode.UP, DOWN, UP, UP, UP, UP);
    private final KeyCombination ctrlF5 = new KeyCodeCombination(KeyCode.F5, UP, DOWN, UP, UP, UP);
    private final KeyCombination ctrlA = new KeyCodeCombination(KeyCode.A, UP, DOWN, UP, UP, UP);

    @FXML
    private FileTableView table;

    @FXML
    private MultiBreadCrumbBar multiBreadCrumbBar;
    @FXML
    private LocalDriveSegmentedButton localDrives;
    @FXML
    private AccountSegmentedButton remoteDrives;
    @FXML
    private DriveSpaceBar driveSpaceBar;
    @FXML
    private StackPane tableStack;

    @FXML
    private SimpleStringProperty side = new SimpleStringProperty();

    private SimpleBooleanProperty remote = new SimpleBooleanProperty(false);
    private SimpleBooleanProperty cloud = new SimpleBooleanProperty(false);

    public FileView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("fileView.fxml"));
        fxmlLoader.setResources(ResourceBundle.getBundle("internationalization/messages", new Locale(ConfigurationBundle.getLanguage())));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        EventBus.subscribeToFileListResponse(this);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        localDrives.setOnAction(this::onLocalDriveAction);
        localDrives.setActive(true);
        initRemoteDrives();
        table.getLocationProperty().addListener((observable, previousFolder, newFolder) -> {
            if (newFolder != null) {
                if (isRemote()) {
                    String activeAccount = remoteDrives.getActiveAccount();
                    if (activeAccount.equals("Cloud")) {
                        final IFile selectedFile = getSelectedFile();
                        if (selectedFile != null && selectedFile.getPath().equals(newFolder)) {
                            activeAccount = selectedFile.getOrigin();
                        }
                    }
                    EventBus.publish(new FileListRequest(hashCode(), activeAccount, getEventPath(newFolder), previousFolder));
                    showLoadingOverlay(resources);
                } else {
                    multiBreadCrumbBar.setAllRemoteCrumbsInactive();
                    multiBreadCrumbBar.setBreadCrumbs(newFolder);
                    table.setLocalFileList(newFolder, previousFolder);
                    driveSpaceBar.update(getLocation());
                }
            } else {
                setLocation(previousFolder);
            }
        });
        multiBreadCrumbBar.remoteProperty().bindBidirectional(remote);
        multiBreadCrumbBar.setOnLocalCrumbAction(this::onLocalCrumbAction);
        multiBreadCrumbBar.setOnRemoteCrumbAction(event -> {
            final RemoteFileBreadCrumbBar activeRemoteCrumb = multiBreadCrumbBar.getActiveRemoteCrumb();
            remoteDrives.select(activeRemoteCrumb.getAccount());
            setLocation(multiBreadCrumbBar.getNewRemotePath(event, remoteDrives.getActiveAccount()));
        });
    }

    private void showLoadingOverlay(ResourceBundle resources) {
        table.setDisable(true);
        localDrives.setDisable(true);
        remoteDrives.setDisable(true);
        multiBreadCrumbBar.setDisable(true);
        setEffect(new GaussianBlur(0.5d));
        final ImageView loadingImage = ImageBundle.getImageView("icon.loading");
        final Label loading = new Label(resources.getString("loading.file.list"), loadingImage);
        loading.setContentDisplay(ContentDisplay.TOP);
        loading.setStyle("-fx-font-size: 15");
        loading.setId("loading");
        tableStack.getChildren().add(0, loading);
    }


    private void hideLoadingOverLay() {
        table.setDisable(false);
        localDrives.setDisable(false);
        remoteDrives.setDisable(false);
        multiBreadCrumbBar.setDisable(false);
        setEffect(null);
        tableStack.getChildren().removeIf(node -> node.getId().equals("loading"));
    }

    public void initStartLocation() {
        Path startLocation = ConfigurationBundle.getStartLocation(side.get());
        setLocation(startLocation);
        getLocationProperty().addListener((observableValue, path, newLocation) -> {
            if (!isRemote()) {
                ConfigurationBundle.setStartLocation(side.get(), newLocation);
            }
        });
        ObservableList<ToggleButton> buttons = localDrives.getButtons();
        buttons.stream().filter(button -> startLocation.startsWith(button.getText())).forEach(button -> button.setSelected(true));
    }

    private void initRemoteDrives() {
        remoteDrives.activeProperty().addListener((observableValue, oldValue, newValue) -> remote.set(newValue));
        remoteDrives.setOnAction(event -> {
            final String account = ((Styleable) event.getSource()).getId();
            setLocation(null);
            setLocation(Paths.get(account));
            driveSpaceBar.clear();
        });
    }

    public void refresh() {
//        table.getItems().clear();
//        final Path location = getLocation();
//        if (isRemote()) {
//            EventBus.publish(new FileListRequest(hashCode(), remoteDrives.getActiveAccount(), getEventPath(location), null));
//        } else {
//            table.setLocalFileList(location, null);
//        }
        setLocation(null);
    }

    @Override
    public void requestFocus() {
        table.requestFocus();
    }

    public void onLocalCrumbAction(BreadCrumbBar.BreadCrumbActionEvent<String> event) {
        remote.set(false);
        final Path newPath = multiBreadCrumbBar.getNewLocalPath(event);
        table.setLocation(newPath);
        deselectButtons(remoteDrives);
        setDefaultStyle();
    }

    @FXML
    public void onMouseClicked(MouseEvent event) {
        final IFile selectedItem = table.getSelectionModel().getSelectedItem();
        if (isDoubleClick(event)) {
            open(selectedItem);
        } else if (MouseButton.SECONDARY.equals(event.getButton())) {
            selectedItem.mark();
        }
    }

    private boolean isDoubleClick(MouseEvent event) {
        return event.getClickCount() == 2 && event.getButton().compareTo(MouseButton.PRIMARY) == 0;
    }

    @FXML
    public void onKeyPressed(final KeyEvent event) {
        final IFile selectedFile = table.getSelectionModel().getSelectedItem();
        if (backSpace.match(event)) {
            table.setLocation(selectedFile.getParentDirectory());
        } else if (enter.match(event)) {
            open(selectedFile);
        } else if (space.match(event)) {
            selectedFile.mark();
        } else if (shiftUp.match(event)) {
            selectedFile.mark();
        } else if (shiftDown.match(event)) {
            selectedFile.mark();
        } else if (ctrlF5.match(event)) {
            refresh();
        } else if (ctrlA.match(event)) {
            table.getItems().stream().forEach(IFile::mark);
        }
    }

    private void open(final IFile selectedFile) {
        if (selectedFile.isDirectory() || selectedFile.isStepBack()) {
            final Path folder = selectedFile.getPath();
            table.setLocation(folder);
        } else if (isLocalFile(selectedFile)) {
            openWithDefaultProgram((LocalFile) selectedFile);
        }
    }

    private boolean isLocalFile(IFile selectedFile) {
        return LocalFile.class.isAssignableFrom(selectedFile.getClass());
    }

    private void openWithDefaultProgram(LocalFile selectedFile) {
        try {
            Desktop.getDesktop().open(selectedFile.getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onLocalDriveAction(ActionEvent event) {
        remote.set(false);
        ToggleButton localDriveButton = (ToggleButton) event.getSource();
        final Path location = Paths.get(localDriveButton.getText());
        localDriveButton.setSelected(true);
        setLocation(location);
        remoteDrives.deselectButtons();
        driveSpaceBar.update(getLocation());
        setDefaultStyle();
    }

    private void setDefaultStyle() {
        table.getStylesheets().clear();
        table.getStylesheets().add("style/HyperCubeTableView.css");
    }

    public IFile getSelectedFile() {
        return table.getSelectionModel().getSelectedItem();
    }

    public Collection<IFile> getMarkedFiles() {
        final Collection<IFile> marked = new ArrayList<>(50);
        final ObservableList<IFile> files = table.getItems();
        marked.addAll(files.stream().filter(IFile::isMarked).collect(Collectors.toList()));
        if (marked.isEmpty()) {
            marked.add(getSelectedFile());
        }
        return marked;
    }

    public boolean isActive() {
        return table.isActive();
    }

    private boolean isRemote() {
        return remote.get();
    }

    public BooleanProperty getActiveProperty() {
        return table.getActiveProperty();
    }

    public void setLocation(Path location) {
        table.setLocation(location);
    }

    public Path getLocation() {
        return table.getLocation();
    }

    public SimpleObjectProperty<Path> getLocationProperty() {
        return table.getLocationProperty();
    }

    public String getSide() {
        return side.get();
    }

    @FXML
    public void setSide(final String side) {
        this.side.set(side);
    }

    public void setRemoteFileList(final FileListResponse event) {
        final Path folder = event.getFolder();
        if (event.isCloud()) {
            if (!cloud.get()) {
                table.getItems().clear();
                cloud.set(true);
            }
            table.setCloudFileList(folder, event.getFileList());
        } else {
            table.setRemoteFileList(event.getAccount(), event.getPreviousFolder(), folder, event.getFileList());
        }
        activateRemoteStorageButton(event);
        deselectButtons(localDrives);
        table.requestFocus();
    }

    private void activateRemoteStorageButton(final FileListResponse event) {
        final ObservableList<ToggleButton> buttons = remoteDrives.getButtons();
        buttons.stream().filter(button -> button.getText().equals(event.getAccount())).forEach(button -> button.setSelected(true));
    }

    public void deselectButtons(final SegmentedButton segmentedButton) {
        final ObservableList<ToggleButton> buttons = segmentedButton.getButtons();
        buttons.stream().filter(ToggleButton::isSelected).forEach(button -> button.setSelected(false));
    }

    public void jumpToFile(final Path filePath) {
        table.setLocation(filePath.getParent());
        final Optional<IFile> destinationFile = table.getItems().parallelStream().filter(file -> file.getName().equals(filePath.getFileName().toString())).findFirst();
        if (destinationFile.isPresent()) {
            final IFile iFile = destinationFile.get();
            table.getSelectionModel().select(iFile);
            table.scrollTo(iFile);
        }
    }

    @Override
    @Handler(rejectSubtypes = true)
    public void onEvent(final FileListResponse event) {
        if (isRemote() && isTarget(event)) {
            Platform.runLater(() -> {
                multiBreadCrumbBar.setRemoteBreadCrumbs(event.getAccount(), event.getFolder());
                hideLoadingOverLay();
                StyleUtil.changeStyle(table, event.getAccount());
                setRemoteFileList(event);
                driveSpaceBar.update(event.getQuotaInfo());
            });
        }
    }

    private boolean isTarget(FileListResponse event) {
        return event.getTarget().equals(hashCode()) || getLocation().equals(event.getFolder());
    }

    public SimpleBooleanProperty remoteProperty() {
        return remote;
    }
}
