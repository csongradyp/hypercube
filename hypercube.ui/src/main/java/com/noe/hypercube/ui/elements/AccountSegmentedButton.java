package com.noe.hypercube.ui.elements;

import com.noe.hypercube.ui.bundle.AccountBundle;
import com.noe.hypercube.ui.bundle.ImageBundle;
import com.noe.hypercube.ui.domain.account.AccountInfo;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import org.controlsfx.control.SegmentedButton;

public class AccountSegmentedButton extends SegmentedButton {

    private SimpleBooleanProperty active = new SimpleBooleanProperty(false);
    private EventHandler<ActionEvent> eventHandler;
    private ButtonHandler onButtonAddedHandler;

    public AccountSegmentedButton() {
        super();
        final ObservableList<AccountInfo> accounts = AccountBundle.getConnectedAccounts();
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

    protected void addListenerForAccountChanges() {
        AccountBundle.getConnectedAccounts().addListener((ListChangeListener<AccountInfo>) change -> {
            while (change.next()) {
                final List<? extends AccountInfo> addedAccount = change.getAddedSubList();
                addedAccount.stream().filter(AccountInfo::isActive).forEach(account -> {
                    final ToggleButton accountStorageButton = createAccountButton(account.getName());
                    getButtons().add(accountStorageButton);
                });
                final List<? extends AccountInfo> removedAccount = change.getRemoved();
                for (AccountInfo account : removedAccount) {
                    getButtons().removeIf(toggleButton -> toggleButton.getText().equals(account.getName()));
                }
            }
        });
    }

    public ToggleButton addButton(final String account, Node icon) {
        final ToggleButton storageButton = createButton(account);
        storageButton.setGraphic(icon);
        getButtons().add(0, storageButton);
        return storageButton;
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
        if(onButtonAddedHandler != null) {
            onButtonAddedHandler.handle(accountStorageButton.getId());
        }
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
        buttons.stream().filter(ToggleButton::isSelected).forEach(button -> button.setSelected(false));
    }

    public void select(final String account) {
        final ObservableList<ToggleButton> accountButtons = getButtons();
        accountButtons.stream().filter(accountButton -> accountButton.getId().equals(account)).forEach(accountButton -> accountButton.setSelected(true));
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

    public void setOnButtonAdded(final ButtonHandler onButtonAddedHandler) {
        this.onButtonAddedHandler = onButtonAddedHandler;
    }

    public interface ButtonHandler {
        public void handle(final String account);
    }
}
