package com.noe.hypercube.ui.bundle;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.application.Platform;
import javafx.collections.ObservableList;

import java.util.ArrayList;

public final class AccountBundle {

    private static final AccountBundle INSTANCE = new AccountBundle();
    private final ObservableList<String> accounts;

    private AccountBundle() {
        accounts = new ObservableListWrapper<>(new ArrayList<>());
    }

    public static ObservableList<String> getAccounts() {
        return INSTANCE.accounts;
    }

    public static void registerAccount(String accountName) {
        Platform.runLater(() -> {
            INSTANCE.accounts.add(accountName);
            HistoryBundle.createSpaceFor(accountName);
        });
    }

}
