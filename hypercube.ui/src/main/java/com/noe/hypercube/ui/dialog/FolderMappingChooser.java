package com.noe.hypercube.ui.dialog;

import com.noe.hypercube.ui.bundle.AccountBundle;
import com.noe.hypercube.ui.bundle.ImageBundle;
import com.sun.javafx.collections.ObservableListWrapper;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import java.nio.file.Path;
import java.util.Optional;


public class FolderMappingChooser extends HBox {

    private final TextField folderPath;
    private final Button folderChooserButton;
    private final ComboBox<String> accountChoice;

    public FolderMappingChooser(final ValidationSupport validationSupport) {
        setSpacing(10.0d);
        accountChoice = new ComboBox<>(new ObservableListWrapper<>(AccountBundle.getAccountNames()));
        accountChoice.setMinWidth(100.0d);
        final Callback<ListView<String>, ListCell<String>> cellFactory = (param) -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setGraphic(ImageBundle.getAccountImageView(item));
                    setText(item);
                } else {
                    setGraphic(null);
                    setText(null);
                }
            }
        };
        accountChoice.setButtonCell(cellFactory.call(null));
        accountChoice.setCellFactory(cellFactory);
        if(!accountChoice.getItems().isEmpty()) {
            accountChoice.setValue(accountChoice.getItems().get(0));
        }
        folderPath = new TextField();
        folderPath.setEditable(false);
        folderPath.prefWidthProperty().bind(widthProperty().subtract(180.0d));
        folderChooserButton = AwesomeDude.createIconButton(AwesomeIcon.FOLDER_ALTPEN);
        folderChooserButton.setOnAction(event -> {
            final String account = accountChoice.getSelectionModel().getSelectedItem();
            if(account != null && !account.isEmpty()) {
                final Optional<Path> remoteFolder = new RemoteFolderChooserDialog(account).showAndWait();
                if(remoteFolder.isPresent()) {
                    setFolderPath(remoteFolder.get());
                }
            }
        });

        accountChoice.setOnAction(event -> folderPath.setText(""));

        getChildren().addAll(accountChoice, folderPath, folderChooserButton);

        validationSupport.registerValidator(folderPath, Validator.createEmptyValidator("Text is required", Severity.ERROR));
        validationSupport.registerValidator(accountChoice, Validator.createEmptyValidator("Text is required", Severity.ERROR));
    }

    public void setFolderPath(final Path remoteFolder) {
        folderPath.setText(remoteFolder.toString());
    }

    public void setAccount(final String account) {
        accountChoice.getSelectionModel().select(account);
    }

    public String getAccount() {
        return accountChoice.getSelectionModel().getSelectedItem();
    }

    public String getFolder() {
        return folderPath.getText();
    }

}
