package com.noe.hypercube.ui.dialog;

import com.noe.hypercube.event.domain.request.MappingRequest;
import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
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

public class AddMappingDialog extends Dialog<MappingRequest> implements Initializable {

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
        validationSupport = new ValidationSupport();
        bundle = ResourceBundle.getBundle("internationalization/messages", new Locale(ConfigurationBundle.getLanguage()));
        FXMLLoader fxmlLoader = new FXMLLoader(AddMappingDialog.class.getClassLoader().getResource("mappingdialog.fxml"), bundle);
        fxmlLoader.setResources(bundle);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        setTitle(bundle.getString("dialog.mapping.add.title"));
        setResizable(true);
        setResultConverter(param -> {
            if (ButtonBar.ButtonData.OK_DONE == param.getButtonData()) {
                return createMappingRequest();
            }
            return null;
        });
    }

    public static java.util.Optional<MappingRequest> showAddDialog() {
        final AddMappingDialog addMappingDialog = new AddMappingDialog();
        return addMappingDialog.showAndWait();
    }

    public static java.util.Optional<MappingRequest> showMapLocalDialog(final Path localFolderPath) {
        final AddMappingDialog addMappingDialog = new AddMappingDialog();
        addMappingDialog.setLocalFolder(localFolderPath);
        return addMappingDialog.showAndWait();
    }

    public static java.util.Optional<MappingRequest> showMapRemoteDialog(final String account, final Path remoteFolder) {
        final AddMappingDialog addMappingDialog = new AddMappingDialog();
        addMappingDialog.setInitialRemoteFolder(account, remoteFolder);
        return addMappingDialog.showAndWait();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupFolderChooser();
        setupAddMappingChooserButton();
        validationSupport.registerValidator(localFolder, true, Validator.createEmptyValidator("Text is required", Severity.ERROR));
//        ValidationSupport.setRequired(localFolder, true);
//        validationSupport.validationResultProperty().addListener( (o, oldValue, newValue) ->  messageList.getItems().setAll(newValue.getMessages()));
        addValidationListener();
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        initialRemoteMappingChooser = new FolderMappingChooser(validationSupport);
        remoteMappings.getChildren().add(initialRemoteMappingChooser);
    }

    private void addValidationListener() {
        final ButtonType buttonType = new ButtonType(bundle.getString("dialog.mapping.action.add"), ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().add(buttonType);
        final ObservableList<Node> children = getDialogPane().getChildren();
        final FilteredList<Node> filtered = children.filtered(node -> ButtonBar.class.isAssignableFrom(node.getClass()));
        final ButtonBar buttonBar = (ButtonBar) filtered.get(0);
        final Node bindButton = buttonBar.getButtons().get(0);
        bindButton.setDisable(true);
        validationSupport.validationResultProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.getErrors().isEmpty() && newValue.getWarnings().isEmpty()) {
                bindButton.setDisable(false);
            } else {
                bindButton.setDisable(true);
            }
            validationSupport.redecorate();
        });
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
        setHeight(getHeight() + 33.0d);
    }

    private void createRemoveButtonFor(FolderMappingChooser folderMappingChooser) {
        final Label remove = AwesomeDude.createIconLabel(AwesomeIcon.MINUS_SQUARE, "15");
        remove.getStylesheets().add("style/labelbutton.css");
        remove.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> remoteMappings.getChildren().removeAll(folderMappingChooser));
        folderMappingChooser.getChildren().add(remove);
    }

    private MappingRequest createMappingRequest() {
        final MappingRequest mappingRequest = new MappingRequest(Paths.get(localFolder.getText()));
        final ObservableList<Node> remoteMappingChoosers = remoteMappings.getChildren();
        for (Node remoteMappingChooser : remoteMappingChoosers) {
            if (FolderMappingChooser.class.isAssignableFrom(remoteMappingChooser.getClass())) {
                FolderMappingChooser mappingChooser = (FolderMappingChooser) remoteMappingChooser;
                final String account = mappingChooser.getAccount();
                final String remoteFolder = mappingChooser.getFolder();
                mappingRequest.addRemoteFolder(account, Paths.get(remoteFolder));
            }
        }
        return mappingRequest;
    }

    private void setupFolderChooser() {
        AwesomeDude.setIcon(localFolderChooser, AwesomeIcon.FOLDER_ALTPEN);
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
