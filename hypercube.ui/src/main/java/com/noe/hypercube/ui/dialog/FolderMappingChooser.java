package com.noe.hypercube.ui.dialog;

import com.noe.hypercube.ui.bundle.AccountBundle;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import java.nio.file.Path;


public class FolderMappingChooser extends HBox {

    private final TextField folderPath;
    private final Button folderChooserButton;
    private final ChoiceBox<String> accountChoice;

    public FolderMappingChooser(final ValidationSupport validationSupport) {
        setSpacing(10.0d);
        accountChoice = new ChoiceBox<>(AccountBundle.getAccounts());
        accountChoice.setMinWidth(100.0d);
        folderPath = new TextField();
        folderPath.prefWidthProperty().bind(widthProperty());
        folderChooserButton = AwesomeDude.createIconButton(AwesomeIcon.FOLDER_OPEN);
        getChildren().addAll(accountChoice, folderPath, folderChooserButton);

        validationSupport.registerValidator(folderPath, Validator.createEmptyValidator("Text is required", Severity.WARNING));
        validationSupport.registerValidator(accountChoice, Validator.createEmptyValidator("Text is required", Severity.WARNING));
    }

    public void setFolderPath(final Path remoteFolder) {
        folderPath.setText(remoteFolder.toString());
    }

    public void setAccount(final String account) {
        accountChoice.getSelectionModel().select(account);
    }
}