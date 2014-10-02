package com.noe.hypercube.ui.elements;

import com.noe.hypercube.ui.bundle.AccountBundle;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.collections.ObservableList;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ToggleButton;
import javafx.util.Callback;
import javafx.util.Pair;
import org.controlsfx.dialog.LoginDialog;

public class ManagedAccountSegmentedButton extends AccountSegmentedButton {

    public ManagedAccountSegmentedButton() {
        super();
        final ObservableList<ToggleButton> buttons = getButtons();
        buttons.add(0, createCloudButton());
        buttons.add(buttons.size() - 1, createAddConnectionButton());
    }

    private ToggleButton createAddConnectionButton() {
        final ToggleButton addConnectionButton = createButton("Add new connection");
        AwesomeDude.setIcon(addConnectionButton, AwesomeIcon.PLUS, ContentDisplay.GRAPHIC_ONLY);
        addConnectionButton.setOnAction(actionEvent -> {
            addConnectionButton.setSelected(false);
            final LoginDialog loginDialog = new LoginDialog(new Pair<>("", ""), new Callback<Pair<String, String>, Void>() {
                @Override
                public Void call(Pair<String, String> stringStringPair) {
                    return null;
                }
            });
            loginDialog.show();
        });
        return addConnectionButton;
    }

    private ToggleButton createCloudButton() {
        final ToggleButton accountStorageButton = createButton("Cloud");
        AwesomeDude.setIcon(accountStorageButton, AwesomeIcon.CLOUD, ContentDisplay.GRAPHIC_ONLY);
        accountStorageButton.setDisable(true);
        AccountBundle.connectedProperty().addListener((observableValue, aBoolean, connected) -> {
            if(connected) {
                accountStorageButton.setDisable(false);
            }
        });
        return accountStorageButton;
    }

}
