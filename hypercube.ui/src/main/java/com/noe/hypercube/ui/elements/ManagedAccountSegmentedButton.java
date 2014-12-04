package com.noe.hypercube.ui.elements;

import com.noe.hypercube.ui.bundle.AccountBundle;
import com.noe.hypercube.ui.bundle.ConfigurationBundle;
import com.noe.hypercube.ui.dialog.AddConnectionDialog;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ToggleButton;

public class ManagedAccountSegmentedButton extends AccountSegmentedButton {

    private final ResourceBundle resourceBundle;

    public ManagedAccountSegmentedButton() {
        super();
        resourceBundle = ResourceBundle.getBundle("internationalization/messages", new Locale(ConfigurationBundle.getLanguage()));
        final ObservableList<ToggleButton> buttons = getButtons();
        buttons.add(0, createCloudButton());
        buttons.add(0, createAddConnectionButton());
    }

    private ToggleButton createAddConnectionButton() {
        final ToggleButton addConnectionButton = createButton(resourceBundle.getString("menu.cloud.add"));
        AwesomeDude.setIcon(addConnectionButton, AwesomeIcon.PLUS, ContentDisplay.GRAPHIC_ONLY);
        addConnectionButton.setOnAction(actionEvent -> {
            addConnectionButton.setSelected(false);
            final AddConnectionDialog addConnectionDialog = new AddConnectionDialog();
            addConnectionDialog.initOwner(getScene().getWindow());
            addConnectionDialog.show();
        });
        return addConnectionButton;
    }

    private ToggleButton createCloudButton() {
        final ToggleButton accountStorageButton = createButton("Cloud");
        AwesomeDude.setIcon(accountStorageButton, AwesomeIcon.CLOUD, ContentDisplay.GRAPHIC_ONLY);
        accountStorageButton.setDisable(!AccountBundle.isAnyAccountActive());
        AccountBundle.connectedProperty().addListener((observableValue, aBoolean, connected) -> {
            if(connected) {
                accountStorageButton.setDisable(false);
            }
        });
        return accountStorageButton;
    }

}
