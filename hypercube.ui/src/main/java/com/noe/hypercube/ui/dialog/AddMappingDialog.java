package com.noe.hypercube.ui.dialog;

import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.domain.MappingRequest;
import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.DialogStyle;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.ResourceBundle;

public class AddMappingDialog extends Dialog implements Initializable {

    @FXML
    private TextField localFolder;
    @FXML
    private VBox remoteMappings;
    @FXML
    private Button localFolderChooser;
    @FXML
    private Label addMappingChooser;

    private FolderMappingChooser initialRemoteMappingChooser;

    private ResourceBundle bundle;
    private ValidationSupport validationSupport;

    public AddMappingDialog() {
        super(null, "", false, DialogStyle.NATIVE);
        validationSupport = new ValidationSupport();
        bundle = ResourceBundle.getBundle("internationalization/messages", new Locale(ConfigurationBundle.getLanguage()));
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("mappingdialog.fxml"), bundle);
        fxmlLoader.setResources(bundle);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static void showAddDialog() {
        final AddMappingDialog addMappingDialog = new AddMappingDialog();
        addMappingDialog.show();
    }

    public static void showMapLocalDialog(final Path localFolderPath) {
        final AddMappingDialog addMappingDialog = new AddMappingDialog();
        addMappingDialog.setLocalFolder(localFolderPath);
        addMappingDialog.show();
    }

    public static void showMapRemoteDialog(final String account, final Path remoteFolder) {
        final AddMappingDialog addMappingDialog = new AddMappingDialog();
        addMappingDialog.setInitialRemoteFolder(account, remoteFolder);
        addMappingDialog.show();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setTitle(bundle.getString("dialog.mapping.add.title"));
        setIconifiable(false);
        setResizable(true);
        setupFolderChooser();
        setupAddMappingChooserButton();
        validationSupport.registerValidator(localFolder, Validator.createEmptyValidator("Text is required", Severity.WARNING));
        final Action bindAction = createBindAction();
        getActions().addAll(bindAction, Actions.CANCEL);
        initialRemoteMappingChooser = new FolderMappingChooser(validationSupport);
        remoteMappings.getChildren().add(initialRemoteMappingChooser);
    }

    private void setupAddMappingChooserButton() {
        AwesomeDude.setIcon(addMappingChooser, AwesomeIcon.PLUS_SQUARE, ContentDisplay.GRAPHIC_ONLY);
        addMappingChooser.getStylesheets().add("style/labelbutton.css");
        addMappingChooser.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> addMappingChooser());
    }

    private void addMappingChooser() {
        final FolderMappingChooser folderMappingChooser = new FolderMappingChooser(validationSupport);
        createRemoveButtonFor(folderMappingChooser);
        remoteMappings.getChildren().add(folderMappingChooser);
    }

    private void createRemoveButtonFor(FolderMappingChooser folderMappingChooser) {
        final Label remove = AwesomeDude.createIconLabel(AwesomeIcon.MINUS_SQUARE, "15");
        remove.getStylesheets().add("style/labelbutton.css");
        remove.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> remoteMappings.getChildren().removeAll(folderMappingChooser));
        folderMappingChooser.getChildren().add(remove);
    }

    private Action createBindAction() {
        return new AbstractAction(bundle.getString("dialog.mapping.action.add")) {
            { ButtonBar.setType(this, ButtonBar.ButtonType.OK_DONE); }

            @Override
            public void handle(ActionEvent event) {
                Dialog dialog = (Dialog) event.getSource();
                if (validationSupport.getValidationResult().getWarnings().isEmpty()) {
                    EventBus.publish(createAddMappingRequest());
                    dialog.hide();
                } else {
                    validationSupport.redecorate();
                }
            }
        };
    }

    private MappingRequest createAddMappingRequest() {
        final MappingRequest mappingRequest = new MappingRequest(Paths.get(localFolder.getText()));
        final ObservableList<Node> remoteMappingChoosers = remoteMappings.getChildren();
        for (Node remoteMappingChooser : remoteMappingChoosers) {
            FolderMappingChooser mappingChooser = (FolderMappingChooser) remoteMappingChooser;
            final String account = mappingChooser.getAccount();
            final String folder = mappingChooser.getFolder();
            mappingRequest.add(account, Paths.get(folder));
        }
        return mappingRequest;
    }

    private void setupFolderChooser() {
        AwesomeDude.setIcon(localFolderChooser, AwesomeIcon.FOLDER_OPEN);
        localFolderChooser.setOnAction(event -> {
            final DirectoryChooser fileChooser = new DirectoryChooser();
            final File folder = fileChooser.showDialog(null);
            if (folder != null) {
                localFolder.setText(folder.toString());
            }
        });
    }

    public void setLocalFolder(final Path localFolderPath) {
        localFolder.setText(localFolderPath.toString());
        localFolder.setDisable(true);
        localFolderChooser.setVisible(false);
    }

    public void setInitialRemoteFolder(final String account, final Path remoteFolder) {
        initialRemoteMappingChooser.setAccount(account);
        initialRemoteMappingChooser.setFolderPath(remoteFolder);
    }
}
