package com.noe.hypercube.ui.dialog;

import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import com.noe.hypercube.ui.domain.file.IFile;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.controlsfx.dialog.Dialog;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Locale;
import java.util.ResourceBundle;

public class FileActionConfirmDialog extends Dialog implements Initializable {

    @FXML
    private Label source;
    @FXML
    private Label destination;
    @FXML
    private Label selectedItemsText;
    @FXML
    private Label itemCount;
    @FXML
    private ListView<IFile> targetList;

    private final Path sourceFolder;
    private final Path destinationFolder;
    private final Collection<IFile> targets;

    public FileActionConfirmDialog(final Object owner, final String title, final Path sourceFolder, final Path destinationFolder, final Collection<IFile> targets) {
        super(owner, title);
        this.sourceFolder = sourceFolder;
        this.destinationFolder = destinationFolder;
        this.targets = targets;
        ResourceBundle bundle = ResourceBundle.getBundle("internationalization/messages", new Locale(ConfigurationBundle.getLanguage()));
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("fileActionConfirmDialog.fxml"), bundle);
        fxmlLoader.setResources(bundle);
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
        source.setText(sourceFolder.toString());
        destination.setText(destinationFolder.toString());
        selectedItemsText.setPrefWidth(calculateWidth());
        itemCount.setText(Integer.toString(targets.size()));
        targetList.getItems().addAll(targets);
        targetList.getSelectionModel().selectedIndexProperty().addListener((observable, oldvalue, newValue) -> Platform.runLater(() -> targetList.getSelectionModel().select(-1)));
        getActions().addAll(ACTION_YES, ACTION_CANCEL);
    }

    private Double calculateWidth() {
        return selectedItemsText.getText().length() * 6.0;
    }
}
