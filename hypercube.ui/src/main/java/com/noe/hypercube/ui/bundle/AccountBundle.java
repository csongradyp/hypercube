package com.noe.hypercube.ui.bundle;

import com.noe.hypercube.ui.domain.account.AccountInfo;
import com.sun.javafx.collections.ObservableListWrapper;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public final class AccountBundle {

    private static final AccountBundle INSTANCE = new AccountBundle();
    private final ObservableList<AccountInfo> accounts;
    private final BooleanProperty connected = new SimpleBooleanProperty(false);

    private AccountBundle() {
        accounts = new ObservableListWrapper<>(new ArrayList<>());
    }

    public static ObservableList<AccountInfo> getAccounts() {
        return INSTANCE.accounts;
    }

    public static void registerAccount(final String accountName, final BooleanProperty attached, final BooleanProperty connected) {
        Platform.runLater(() -> {
            HistoryBundle.createSpaceFor(accountName);
            INSTANCE.accounts.add(new AccountInfo(accountName, attached, connected));
            if(connected.get()) {
                INSTANCE.connected.set(true);
            }
        });
    }

    public static List<String> getAccountNames() {
        return getAccounts().stream().map(AccountInfo::getName).collect(Collectors.toList());
    }

    public static List<String> getConnectedAccountNames() {
        return getAccounts().stream().filter(AccountInfo::isConnected).map(AccountInfo::getName).collect(Collectors.toList());
    }

    private void activate(final String accountName) {
        activate(accountName, true);
    }

    private void deactivate(final String accountName) {
        activate(accountName, false);
    }

    private void activate(final String accountName, final Boolean active) {
        accounts.stream().filter(account -> account.getName().equals(accountName)).forEach(account -> account.setConnected(active));
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

    public static BooleanProperty connectedProperty() {
        return INSTANCE.connected;
    }
}
