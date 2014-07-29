package com.noe.hypercube.ui;

import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import com.noe.hypercube.ui.domain.IFile;
import com.noe.hypercube.ui.factory.IconFactory;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.controlsfx.control.BreadCrumbBar;
import org.controlsfx.control.SegmentedButton;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static javafx.scene.input.KeyCombination.ModifierValue.DOWN;
import static javafx.scene.input.KeyCombination.ModifierValue.UP;

public class FileView extends VBox implements Initializable {

    private static final String SEPARATOR = System.getProperty("file.separator");
    private static final String SEPARATOR_PATTERN = Pattern.quote(SEPARATOR);

    private final KeyCombination enter = new KeyCodeCombination(KeyCode.ENTER);
    private final KeyCombination backSpace = new KeyCodeCombination(KeyCode.BACK_SPACE);
    private final KeyCombination space = new KeyCodeCombination(KeyCode.SPACE);
    private final KeyCombination shiftDown = new KeyCodeCombination(KeyCode.DOWN, DOWN, UP, UP, UP, UP);
    private final KeyCombination shiftUp = new KeyCodeCombination(KeyCode.UP, DOWN, UP, UP, UP, UP);

    @FXML
    private FileTableView table;

    @FXML
    private BreadCrumbBar<String> breadcrumb;
    @FXML
    private SegmentedButton localDrives;
    @FXML
    private SegmentedButton remoteDrives;

    @FXML
    private Label metaDataInfo;
    @FXML
    private SimpleStringProperty side = new SimpleStringProperty();

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
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initLocalDrives();
        initRemoteDrives();
        disableBreadcrumbFocusTraversal();
        table.getLocationProperty().addListener((observable, oldValue, newValue) -> {
            setBreadCrumb(newValue);
            table.updateLocation(newValue);
        });
        table.getActiveProperty().addListener((observable, oldValue, newValue) -> table.getSelectionModel().selectFirst());
        //        MasterDetailPane pane = new MasterDetailPane();
        //        pane.setMasterNode(table);
        //        pane.setDetailNode(breadcrumb);
        //        pane.setDetailSide( Side.TOP);
        //        pane.setShowDetailNode(true);
    }

    public void initStartLocation() {
        Path startLocation = ConfigurationBundle.getStartLocation(side.get());
        setLocation(startLocation);
        getLocationProperty().addListener((observableValue, path, newLocation) -> ConfigurationBundle.setStartLocation(side.get(), newLocation));
        ObservableList<ToggleButton> buttons = localDrives.getButtons();
        for (ToggleButton button : buttons) {
            if (startLocation.startsWith(button.getText())) {
                button.setSelected(true);
            }
        }
    }

    private void setBreadCrumb(Path path) {
        TreeItem<String> model = BreadCrumbBar.buildTreeModel(path.toString().split(SEPARATOR_PATTERN));
        breadcrumb.setSelectedCrumb(model);
    }

    private void disableBreadcrumbFocusTraversal() {
        breadcrumb.setFocusTraversable(false);
        Callback<TreeItem<String>, Button> crumbFactory = breadcrumb.getCrumbFactory();
        breadcrumb.setCrumbFactory((param) -> {
            Button crumbButton = crumbFactory.call(param);
            crumbButton.setFocusTraversable(false);
            return crumbButton;
        });
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
        button.setOnMouseClicked(event -> {
            table.setLocation(Paths.get(button.getText()));
            button.setSelected(true);
        });
        return button;
    }

    private void initRemoteDrives() {
        for (int i = 0; i < 3; i++) {
            final ToggleButton test = new ToggleButton(String.valueOf(i));
            remoteDrives.getButtons().add(test);
            test.selectedProperty().addListener((observable, oldValue, newValue) -> {
                table.getStylesheets().removeAll("style/darkTheme.css", "style/caspian_mod.css", "style/fileTableView.css");
                if (test.getText().equals("0")) {
                    table.getStylesheets().add("style/darkTheme.css");
                    table.getStylesheets().add("style/fileTableView.css");
                } else if (test.getText().equals("1")) {
                    table.getStylesheets().add("style/caspian_mod.css");
                    table.getStylesheets().add("style/fileTableView.css");
                } else if (test.getText().equals("2")) {
                    table.getStylesheets().add("style/win7.css");
                    table.getStylesheets().add("style/fileTableView.css");
                }
            });
        }
        ToggleButton remoteDriveButton = new ToggleButton("+");
        remoteDriveButton.setFocusTraversable(false);
        remoteDriveButton.setTooltip(new Tooltip("Add new remote drive"));
        remoteDrives.getButtons().add(remoteDriveButton);
        remoteDriveButton.setOnMouseClicked(event -> {
            ToggleButton newRemoteDrive = new ToggleButton("New");
            newRemoteDrive.setFocusTraversable(false);
            remoteDrives.getButtons().add(0, newRemoteDrive);
            remoteDriveButton.setSelected(false);
        });
    }

    @FXML
    public void onCrumbAction(BreadCrumbBar.BreadCrumbActionEvent<String> event) {
        TreeItem<String> selectedCrumb = event.getSelectedCrumb();
        List<String> folders = new ArrayList<>();
        while (selectedCrumb != null) {
            folders.add(0, selectedCrumb.getValue());
            selectedCrumb = selectedCrumb.getParent();
        }
        String path = "";
        for (String folder : folders) {
            path += folder + SEPARATOR;
        }
        table.setLocation(Paths.get(path));
        table.requestFocus();
    }

    @FXML
    public void onMouseClicked(MouseEvent event) {
        if (isDoubleClick(event)) {
            IFile selectedItem = table.getSelectionModel().getSelectedItem();
            stepInto(selectedItem);
        } else if (MouseButton.SECONDARY.equals(event.getButton())) {
            IFile selectedItem = table.getSelectionModel().getSelectedItem();
            selectedItem.mark();
        }
    }

    private boolean isDoubleClick(MouseEvent event) {
        return event.getClickCount() == 2 && event.getButton().compareTo(MouseButton.PRIMARY) == 0;
    }

    @FXML
    public void onKeyPressed(KeyEvent event) {
        IFile selectedFile = table.getSelectionModel().getSelectedItem();

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
        }
    }

    @FXML
    public void onLocalDriveMouseClicked(MouseEvent event) {
        ToggleButton source = (ToggleButton) event.getSource();
        table.updateLocation(Paths.get(source.getText()));
        if (!source.isSelected()) {
            source.setSelected(true);
        }
    }

    private void stepInto(IFile selectedFile) {
        if (selectedFile.isDirectory()) {
            table.setLocation(selectedFile.getPath());
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

    @FXML
    public void setActive(boolean active) {
        table.setActive(active);
    }

    public boolean isActive() {
        return table.isActive();
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
    public void setSide(String side) {
        this.side.set(side);
    }
}
