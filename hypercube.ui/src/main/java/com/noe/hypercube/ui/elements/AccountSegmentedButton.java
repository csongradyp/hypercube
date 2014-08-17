package com.noe.hypercube.ui.elements;

import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.domain.FileListRequest;
import com.noe.hypercube.ui.bundle.AccountBundle;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ToggleButton;
import org.controlsfx.control.SegmentedButton;

import java.util.List;

public class AccountSegmentedButton extends SegmentedButton {

    private SimpleBooleanProperty active = new SimpleBooleanProperty(false);

    public AccountSegmentedButton() {
        super();
        final ObservableList<String> accounts = AccountBundle.getAccounts();
        if(!accounts.isEmpty()) {
            for (String account : accounts) {
                final ToggleButton accountStorageButton = createAccountButton(account);
                getButtons().add(accountStorageButton);
            }
        }
        addListenerForAccountChanges();
    }

    private void addListenerForAccountChanges() {
        AccountBundle.getAccounts().addListener((ListChangeListener<String>) change -> {
            while (change.next()) {
                final List<? extends String> addedAccount = change.getAddedSubList();
                for (String account : addedAccount) {
                    final ToggleButton accountStorageButton = createAccountButton(account);
                    getButtons().add(accountStorageButton);
                }
                final List<? extends String> removedAccount = change.getRemoved();
                for (String account : removedAccount) {
                    getButtons().removeIf(toggleButton -> toggleButton.getText().equals(account));
                }
            }
        });
    }

    private ToggleButton createAccountButton(String account) {
        final ToggleButton accountStorageButton = new ToggleButton(account);
        accountStorageButton.setFocusTraversable(false);
        accountStorageButton.setOnAction(event -> {
            active.set(true);
            EventBus.publish(new FileListRequest(account));
        });
        return accountStorageButton;
    }

    public String getActiveAccount() {
        final ObservableList<ToggleButton> buttons = getButtons();
        for (ToggleButton button : buttons) {
            if (button.isSelected()) {
                return button.getText();
            }
        }
        return "";
    }

    public void deselectButtons() {
        active.set(false);
        final ObservableList<ToggleButton> buttons = getButtons();
        for (ToggleButton button : buttons) {
            if (button.isSelected()) {
                button.setSelected(false);
            }
        }
    }

    public boolean isActive() {
        return active.get();
    }

    public SimpleBooleanProperty activeProperty() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active.set(active);
    }
}
