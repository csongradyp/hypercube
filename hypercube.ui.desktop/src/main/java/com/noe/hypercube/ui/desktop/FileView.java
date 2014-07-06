package com.noe.hypercube.ui.desktop;

import com.noe.hypercube.ui.desktop.domain.File;
import com.noe.hypercube.ui.desktop.domain.IFile;
import com.noe.hypercube.ui.desktop.domain.LocalFile;
import com.noe.hypercube.ui.desktop.factory.FormattedTableCellFactory;
import com.noe.hypercube.ui.desktop.factory.IconFactory;
import com.noe.hypercube.ui.desktop.util.FileSizeCalculator;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.apache.commons.io.FilenameUtils;
import org.controlsfx.control.BreadCrumbBar;
import org.controlsfx.control.MasterDetailPane;
import org.controlsfx.control.SegmentedButton;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class FileView extends VBox implements Initializable {

    private static final String SEPARATOR = System.getProperty("file.separator");
    private static final String SEPARATOR_PATTERN = Pattern.quote(SEPARATOR);

    @FXML
    private TableView<IFile> table;
    @FXML
    private TableColumn<IFile, String> extColumn;
    @FXML
    private TableColumn<IFile, String> fileNameColumn;
    @FXML
    private TableColumn<IFile, String> fileSizeColumn;
    @FXML
    private TableColumn<IFile, String> dateColumnRight;

    @FXML
    private BreadCrumbBar<String> breadcrumb;
    @FXML
    private SegmentedButton localDrives;
    @FXML
    private SegmentedButton removableDrives;
    @FXML
    private SegmentedButton remoteDrives;

    @FXML
    private Label metaDataInfo;

    private final SimpleObjectProperty<Path> location = new SimpleObjectProperty<>();

    private final SimpleBooleanProperty selected = new SimpleBooleanProperty(false);

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
        this.location.addListener((observable, oldValue, newValue) -> {
            TreeItem<String> model = BreadCrumbBar.buildTreeModel(newValue.toString().split(SEPARATOR_PATTERN));
            breadcrumb.setSelectedCrumb(model);
            updateTableModel(newValue);
            table.getSelectionModel().selectFirst();
        });
        this.location.set(Paths.get("C:"));
        selected.addListener((observable, oldValue, newValue) -> table.getSelectionModel().selectFirst());
        MasterDetailPane pane = new MasterDetailPane();
        pane.setMasterNode(table);
        pane.setDetailNode(breadcrumb);
        pane.setDetailSide(Side.TOP);
        pane.setShowDetailNode(true);
    }

    private void setupLocalDrives() {
        List<ToggleButton> drives = collectLocalDrives();
        localDrives.getButtons().addAll(drives);
        localDrives.getButtons().get(0).setSelected(true);
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

    public void updateTableModel(Path dir) {
        java.io.File[] list = dir.toFile().listFiles();
        Collection<IFile> files = new ArrayList<>(100);
        Collection<IFile> dirs = new ArrayList<>(100);
        IFile stepBack = createStepBackFile(dir);
        dirs.add(stepBack);
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
        table.setItems(data);
    }

    private IFile createStepBackFile(Path dir) {
        if (dir.toFile().getParentFile() != null) {
            java.io.File parentFile = dir.toFile().getParentFile();
            File parent = new LocalFile(parentFile);
            parent.setStepBack(true);
            return parent;
        }
        LocalFile localFile = new LocalFile("C:");
        localFile.setStepBack(true);
        return localFile;
    }

    private boolean isStepBack(TableColumn.CellDataFeatures<IFile, String> param) {
        return param.getValue().isStepBack();
    }

    private List<ToggleButton> collectLocalDrives() {
        List<ToggleButton> drives = new ArrayList<>(5);
        Iterable<Path> rootDirectories = FileSystems.getDefault().getRootDirectories();
        for (Path root : rootDirectories) {
            ToggleButton button = new ToggleButton(root.toString(), new ImageView(IconFactory.getStorageIcon(root)));
            button.setOnMouseClicked(event -> {
                setLocation(Paths.get(button.getText()));
                button.setSelected(true);
            });
            drives.add(button);
        }
        return drives;
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
        setLocation(Paths.get(path));
        table.requestFocus();
    }

    @FXML
    public void onMouseClicked(MouseEvent event) {
        if (isDoubleClick(event)) {
            IFile selectedItem = table.getSelectionModel().getSelectedItem();
            stepInto(selectedItem);
        }
    }

    @FXML
    public void onKeyPressed(KeyEvent event) {
        IFile selectedFile = table.getSelectionModel().getSelectedItem();

        if (event.getCode() == KeyCode.BACK_SPACE) {
            if (selectedFile.isStepBack()) {
                setLocation(selectedFile.getPath());
            } else if (!selectedFile.isRoot()) {
                setLocation(selectedFile.getParentFile().getParent());
            }
        }
        else if (event.getCode() == KeyCode.ENTER) {
            stepInto(selectedFile);
        }
        else if (event.getCode() == KeyCode.SPACE) {
            if (selectedFile.isSelected()) {
                selectedFile.setSelected(false);
            }
            else {
                selectedFile.setSelected(true);
            }
        }
    }

    @FXML
    public void onLocalDriveMouseClicked(MouseEvent event) {
        ToggleButton source = (ToggleButton) event.getSource();
        updateTableModel(Paths.get(source.getText()));
        if (!source.isSelected()) {
            source.setSelected(true);
        }
    }

    private void stepInto(IFile selectedFile) {
        if (selectedFile.isDirectory()) {
            location.set(selectedFile.getPath());
        } else {
            System.out.println(selectedFile);
        }
    }

    private boolean isDoubleClick(MouseEvent event) {
        return event.getClickCount() == 2 && event.getButton().compareTo(MouseButton.PRIMARY) == 0;
    }

    @FXML
    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public boolean isSelected() {
        return selected.get();
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

    public Path getActiveDirectory() {
        IFile IFile = table.getSelectionModel().getSelectedItem();
        return IFile.getPath().getParent();
    }
}
