package com.noe.hypercube.ui.dialog;

import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.domain.request.AccountConnectionRequest;
import com.noe.hypercube.ui.bundle.AccountBundle;
import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import com.noe.hypercube.ui.bundle.ImageBundle;
import com.noe.hypercube.ui.elements.AccountChooser;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;

public class AddConnectionDialog extends Dialog<Boolean> implements Initializable {

    @FXML
    private AccountChooser accountChooser;

    private ResourceBundle bundle;

    public AddConnectionDialog() {
        bundle = ResourceBundle.getBundle("internationalization/messages", new Locale(ConfigurationBundle.getLanguage()));
        FXMLLoader fxmlLoader = new FXMLLoader(AddConnectionDialog.class.getClassLoader().getResource("addConnectionDialog.fxml"), bundle);
        fxmlLoader.setResources(bundle);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        setTitle(bundle.getString("menu.cloud.add"));
        final List<String> detachedAccountNames = AccountBundle.getDetachedAccountNames();
        accountChooser.setItems(FXCollections.observableList(detachedAccountNames));
        setResultConverter(param -> {
            if (ButtonBar.ButtonData.OK_DONE == param.getButtonData()) {
                final String account = accountChooser.getSelectionModel().getSelectedItem();
                EventBus.publish(new AccountConnectionRequest(account));
                return true;
            }
            return false;
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setHeaderText(resources.getString("dialog.addconnection.header"));
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        setIcon();
    }

    private void setIcon() {
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        stage.getIcons().add(ImageBundle.getImage("tray.default"));
    }

}
