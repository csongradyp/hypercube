package com.noe.hypercube.ui.bundle;

import com.noe.hypercube.ui.domain.account.AccountInfo;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public final class AccountBundle {

    private static final AccountBundle INSTANCE = new AccountBundle();
    private final ObservableList<AccountInfo> accounts;
    private final SimpleBooleanProperty connected = new SimpleBooleanProperty(false);

    private AccountBundle() {
        accounts = new ObservableListWrapper<>(new ArrayList<>());
    }

    public static ObservableList<AccountInfo> getAccounts() {
        return INSTANCE.accounts;
    }

    public static void registerAccount(final String accountName, final BooleanProperty active) {
        Platform.runLater(() -> {
            HistoryBundle.createSpaceFor(accountName);
            INSTANCE.accounts.add(new AccountInfo(accountName, active));
            INSTANCE.connected.bind(active);
        });
    }

    public static List<String> getAccountNames() {
        final List<String> accountNames = new ArrayList<>();
        for (final AccountInfo accountInfo : getAccounts()) {
            accountNames.add(accountInfo.getName());
        }
        return accountNames;
    }

    private void activate(final String accountName) {
        activate(accountName, true);
    }

    private void deactivate(final String accountName) {
        activate(accountName, false);
    }

    private void activate(final String accountName, final Boolean active) {
        for (AccountInfo account : accounts) {
            if(account.getName().equals(accountName)) {
                account.setActive(active);
            }
        }
    }

    public Boolean isActive(final String accountName) {
        for (AccountInfo account : accounts) {
            if(account.getName().equals(accountName)) {
                return account.isActive();
            }
        }
        throw new IllegalArgumentException(String.format("Unknown account: %s", accountName));
    }

    public static Boolean isAnyAccountActive() {
        return INSTANCE.connected.get();
    }

    public static SimpleBooleanProperty connectedProperty() {
        return INSTANCE.connected;
    }
}
