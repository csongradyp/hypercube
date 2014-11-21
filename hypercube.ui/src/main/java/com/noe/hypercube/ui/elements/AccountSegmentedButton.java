package com.noe.hypercube.ui.elements;

import com.noe.hypercube.ui.bundle.AccountBundle;
import com.noe.hypercube.ui.bundle.ImageBundle;
import com.noe.hypercube.ui.domain.account.AccountInfo;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import org.controlsfx.control.SegmentedButton;

import java.util.List;

public class AccountSegmentedButton extends SegmentedButton {

    private SimpleBooleanProperty active = new SimpleBooleanProperty(false);
    private EventHandler<ActionEvent> eventHandler;

    public AccountSegmentedButton() {
        super();
        final ObservableList<AccountInfo> accounts = AccountBundle.getAccounts();
        final ObservableList<ToggleButton> buttons = getButtons();
        if (!accounts.isEmpty()) {
            for (AccountInfo account : accounts) {
                final ToggleButton accountStorageButton = createAccountButton(account.getName());
                accountStorageButton.setMaxHeight(getHeight());
                buttons.add(accountStorageButton);
            }
        }
        addListenerForAccountChanges();
    }

    private void addListenerForAccountChanges() {
        AccountBundle.getAccounts().addListener((ListChangeListener<AccountInfo>) change -> {
            while (change.next()) {
                final List<? extends AccountInfo> addedAccount = change.getAddedSubList();
                for (AccountInfo account : addedAccount) {
                    if (account.isActive()) {
                        final ToggleButton accountStorageButton = createAccountButton(account.getName());
                        getButtons().add(accountStorageButton);
                    }
                }
                final List<? extends AccountInfo> removedAccount = change.getRemoved();
                for (AccountInfo account : removedAccount) {
                    getButtons().removeIf(toggleButton -> toggleButton.getText().equals(account.getName()));
                }
            }
        });
    }

    private ToggleButton createAccountButton(final String account) {
        final ToggleButton accountStorageButton = createButton(account);
        accountStorageButton.setGraphic(ImageBundle.getAccountImageView(account));
        return accountStorageButton;
    }

    protected ToggleButton createButton(final String account) {
        final ToggleButton accountStorageButton = new ToggleButton();
        accountStorageButton.setMinHeight(25.0);
        accountStorageButton.setMaxHeight(25.0);
        accountStorageButton.setId(account);
        accountStorageButton.setTooltip(new Tooltip(account));
        accountStorageButton.setFocusTraversable(false);
        accountStorageButton.setOnAction(event -> {
            active.set(true);
            accountStorageButton.setSelected(true);
            if (eventHandler != null) {
                eventHandler.handle(event);
            }
        });
        return accountStorageButton;
    }

    public void setOnAction(final EventHandler<ActionEvent> eventHandler) {
        this.eventHandler = eventHandler;
    }

    public String getActiveAccount() {
        final ObservableList<ToggleButton> buttons = getButtons();
        for (ToggleButton button : buttons) {
            if (button.isSelected()) {
                return button.getId();
            }
        }
        throw new RuntimeException("no accounts active");
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

    public void select(final String account) {
        final ObservableList<ToggleButton> accountButtons = getButtons();
        for (ToggleButton accountButton : accountButtons) {
            if(accountButton.getId().equals(account)) {
                accountButton.setSelected(true);
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
