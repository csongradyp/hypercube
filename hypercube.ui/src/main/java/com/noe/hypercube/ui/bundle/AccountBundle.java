package com.noe.hypercube.ui.bundle;

import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.EventHandler;
import com.noe.hypercube.event.domain.AccountEvent;
import net.engio.mbassy.listener.Handler;

import java.util.ArrayList;
import java.util.List;

public final class AccountBundle implements EventHandler<AccountEvent> {

    private static final AccountBundle instance = new AccountBundle();
    private final List<String> accounts;

    private AccountBundle() {
        accounts = new ArrayList<>();
        EventBus.subscribeToAccountEvent( this );
    }

    public static List<String> getAccounts() {
        return instance.accounts;
    }

    public static void registerAccount(String accountName) {
        instance.accounts.add(accountName);
        HistoryBundle.createSpaceFor( accountName );
    }

    @Override
    @Handler(rejectSubtypes = true)
    public void onEvent(AccountEvent event) {
        final String accountName = event.getAccountName();
        accounts.add( accountName );
        HistoryBundle.createSpaceFor( accountName );
    }

}
