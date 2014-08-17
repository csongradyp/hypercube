package com.noe.hypercube.ui;

import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.EventHandler;
import com.noe.hypercube.event.domain.FileListRequest;
import com.noe.hypercube.event.domain.FileListResponse;
import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import com.noe.hypercube.ui.domain.IFile;
import com.noe.hypercube.ui.elements.AccountSegmentedButton;
import com.noe.hypercube.ui.factory.IconFactory;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.VBox;
import net.engio.mbassy.listener.Handler;
import org.controlsfx.control.BreadCrumbBar;
import org.controlsfx.control.SegmentedButton;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static javafx.scene.input.KeyCombination.ModifierValue.DOWN;
import static javafx.scene.input.KeyCombination.ModifierValue.UP;

public class FileView extends VBox implements Initializable, EventHandler<FileListResponse> {

    private final KeyCombination enter = new KeyCodeCombination(KeyCode.ENTER);
    private final KeyCombination backSpace = new KeyCodeCombination(KeyCode.BACK_SPACE);
    private final KeyCombination space = new KeyCodeCombination(KeyCode.SPACE);
    private final KeyCombination shiftDown = new KeyCodeCombination(KeyCode.DOWN, DOWN, UP, UP, UP, UP);
    private final KeyCombination shiftUp = new KeyCodeCombination(KeyCode.UP, DOWN, UP, UP, UP, UP);
    private final KeyCombination ctrlF5 = new KeyCodeCombination(KeyCode.F5, UP, DOWN, UP, UP, UP);

    @FXML
    private FileTableView table;

    @FXML
    private MultiBreadCrumbBar multiBreadCrumbBar;
    @FXML
    private SegmentedButton localDrives;
    @FXML
    private AccountSegmentedButton remoteDrives;

    @FXML
    private Label metaDataInfo;
    @FXML
    private SimpleStringProperty side = new SimpleStringProperty();

    private SimpleBooleanProperty remote = new SimpleBooleanProperty(false);

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
        initLocalDrives();
        initRemoteDrives();
        table.getLocationProperty().addListener((observable, oldValue, newValue) -> {
            if (isRemote()) {
                EventBus.publish(new FileListRequest(remoteDrives.getActiveAccount(), newValue));
            } else {
                multiBreadCrumbBar.setBreadCrumbs(newValue);
                table.setLocalFileList(newValue);
            }
        });
        multiBreadCrumbBar.setOnLocalCrumbAction(this::onLocalCrumbAction);
        multiBreadCrumbBar.setOnRemoteCrumbAction(event -> remote.set(true));
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
        for (ToggleButton button : buttons) {
            if (startLocation.startsWith(button.getText())) {
                button.setSelected(true);
            }
        }
    }

    private void initLocalDrives() {
        List<ToggleButton> drives = collectLocalDrives();
        localDrives.getButtons().addAll(drives);
        localDrives.getButtons().get(0).setSelected(true);
    }

    private List<ToggleButton> collectLocalDrives() {
        List<ToggleButton> drives = new ArrayList<>(5);
        Iterable<Path> rootDirectories = FileSystems.getDefault().getRootDirectories();
        for (Path root : rootDirectories) {
            drives.add(createLocalStorageButton(root));
        }
        return drives;
    }

    private ToggleButton createLocalStorageButton(Path root) {
        ToggleButton button = new ToggleButton(root.toString(), new ImageView(IconFactory.getStorageIcon(root)));
        button.setFocusTraversable(false);
        button.setOnAction(this::onLocalDriveAction);
        return button;
    }

    private void initRemoteDrives() {
        //        for (int i = 0; i < 3; i++) {
        //            final ToggleButton test = new ToggleButton(String.valueOf(i));
        //            remoteDrives.getButtons().add(test);
        //            test.selectedProperty().addListener((observable, oldValue, newValue) -> {
        //                table.getStylesheets().removeAll("style/darkTheme.css", "style/caspian_mod.css", "style/fileTableView.css");
        //                if (test.getText().equals("0")) {
        //                    table.getStylesheets().add("style/darkTheme.css");
        //                    table.getStylesheets().add("style/fileTableView.css");
        //                } else if (test.getText().equals("1")) {
        //                    table.getStylesheets().add("style/caspian_mod.css");
        //                    table.getStylesheets().add("style/fileTableView.css");
        //                } else if (test.getText().equals("2")) {
        //                    table.getStylesheets().add("style/win7.css");
        //                    table.getStylesheets().add("style/fileTableView.css");
        //                }
        //            });
        //        }
        remoteDrives.activeProperty().addListener((observableValue, oldValue, newValue) -> {
            remote.set(newValue);
        });
    }

    public void refresh() {
        table.getItems().clear();
        if (isRemote()) {
            EventBus.publish(new FileListRequest(remoteDrives.getActiveAccount(), getLocation()));
        } else {
            table.setLocalFileList(getLocation());
        }
    }

    public void onLocalCrumbAction(BreadCrumbBar.BreadCrumbActionEvent<String> event) {
        remote.set(false);
        final Path newPath = multiBreadCrumbBar.getNewLocalPath(event);
        table.setLocation(newPath);
        deselectButtons(remoteDrives);
    }

    @FXML
    public void onMouseClicked(MouseEvent event) {
        final IFile selectedItem = table.getSelectionModel().getSelectedItem();
        if (isDoubleClick(event)) {
            stepInto(selectedItem);
        } else if (MouseButton.SECONDARY.equals(event.getButton())) {
            selectedItem.mark();
        }
//        else {
//            table.getSelectionModel().select(event.);
//        }
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
            stepInto(selectedFile);
        } else if (space.match(event)) {
            selectedFile.mark();
        } else if (shiftUp.match(event)) {
            selectedFile.mark();
            table.getSelectionModel().selectAboveCell();
        } else if (shiftDown.match(event)) {
            selectedFile.mark();
            table.getSelectionModel().selectBelowCell();
        } else if (ctrlF5.match(event)) {
            refresh();
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
    }

    private void stepInto(IFile selectedFile) {
        if (selectedFile.isDirectory() || selectedFile.isStepBack()) {
            final Path folder = selectedFile.getPath();
            table.setLocation(folder);
        } else {
            System.out.println(selectedFile);
        }
    }

    public IFile getSelectedFile() {
        return table.getSelectionModel().getSelectedItem();
    }

    public Collection<IFile> getMarkedFiles() {
        ArrayList<IFile> marked = new ArrayList<>(50);
        ObservableList<IFile> files = table.getItems();
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
        table.setRemoteFileList(folder, event.getFileList());
        setLocation(folder);
        activateRemoteStorageButton(event);
        deselectButtons(localDrives);
        table.requestFocus();
    }

    private void activateRemoteStorageButton(final FileListResponse event) {
        final ObservableList<ToggleButton> buttons = remoteDrives.getButtons();
        for (ToggleButton button : buttons) {
            if (button.getText().equals(event.getAccount())) {
                button.setSelected(true);
            }
        }
    }

    public void deselectButtons(final SegmentedButton segmentedButton) {
        final ObservableList<ToggleButton> buttons = segmentedButton.getButtons();
        for (ToggleButton button : buttons) {
            if (button.isSelected()) {
                button.setSelected(false);
            }
        }
    }

    @Override
    @Handler(rejectSubtypes = true)
    public void onEvent(FileListResponse event) {
        if (isRemote()) {
            Platform.runLater(() -> {
                multiBreadCrumbBar.setRemoteBreadCrumbs(event.getAccount(), event.getFolder());
                setRemoteFileList(event);
            });
        }
    }

    public SimpleBooleanProperty remoteProperty() {
        return remote;
    }
}
