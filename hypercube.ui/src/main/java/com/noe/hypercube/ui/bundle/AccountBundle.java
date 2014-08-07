package com.noe.hypercube.ui.bundle;

import java.util.ArrayList;
import java.util.List;

public final class AccountBundle {

    private static final AccountBundle INSTANCE = new AccountBundle();
    private final List<String> accounts;

    private AccountBundle() {
        accounts = new ArrayList<>();
    }

    public static List<String> getAccounts() {
        return INSTANCE.accounts;
    }

    public static void registerAccount(String accountName) {
        INSTANCE.accounts.add(accountName);
        HistoryBundle.createSpaceFor(accountName);
    }

}
