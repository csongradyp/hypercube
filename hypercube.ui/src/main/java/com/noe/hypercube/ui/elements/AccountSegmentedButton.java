package com.noe.hypercube.ui.elements;

import com.noe.hypercube.ui.bundle.AccountBundle;
import com.noe.hypercube.ui.bundle.ImageBundle;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import org.controlsfx.control.SegmentedButton;

import java.util.List;

public class AccountSegmentedButton extends SegmentedButton {

    private SimpleBooleanProperty active = new SimpleBooleanProperty(false);
    private EventHandler<ActionEvent> eventHandler;

    public AccountSegmentedButton() {
        super();
        final ObservableList<String> accounts = AccountBundle.getAccounts();
        if(!accounts.isEmpty()) {
            for (String account : accounts) {
                final ToggleButton accountStorageButton = createAccountButton(account);
                accountStorageButton.setMaxHeight(getHeight());
                getButtons().add(accountStorageButton);
            }
        }
        final ToggleButton cloudButton = createCloudButton();
        getButtons().add(0, cloudButton);
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
        final ToggleButton accountStorageButton = createButton(account);
        accountStorageButton.setGraphic(ImageBundle.getAccountImageView(account));
        return accountStorageButton;
    }

    private ToggleButton createCloudButton() {
        final ToggleButton accountStorageButton = createButton("Cloud");
        AwesomeDude.setIcon(accountStorageButton, AwesomeIcon.CLOUD, ContentDisplay.GRAPHIC_ONLY);
        return accountStorageButton;
    }

    private ToggleButton createButton(String account) {
        final ToggleButton accountStorageButton = new ToggleButton();
        accountStorageButton.setMinHeight(25.0);
        accountStorageButton.setMaxHeight(25.0);
        accountStorageButton.setId(account);
        accountStorageButton.setTooltip(new Tooltip(account));
        accountStorageButton.setFocusTraversable(false);
        accountStorageButton.setOnAction(event -> {
            active.set(true);
            accountStorageButton.setSelected(true);
            if(eventHandler != null) {
                eventHandler.handle(event);
            }
        });
        return accountStorageButton;
    }

    public void setOnAction(EventHandler<ActionEvent> eventHandler) {
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
