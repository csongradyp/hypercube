package com.noe.hypercube.ui.bundle;

import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.EventHandler;
import com.noe.hypercube.event.domain.response.AccountConnectionResponse;
import com.noe.hypercube.ui.domain.account.AccountInfo;
import com.sun.javafx.collections.ObservableListWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import net.engio.mbassy.listener.Handler;

public final class AccountBundle implements EventHandler<AccountConnectionResponse> {

    private static final AccountBundle INSTANCE = new AccountBundle();
    private final ObservableList<AccountInfo> accounts;
    private final ObservableList<AccountInfo> connectedAccounts;
    private final BooleanProperty connected = new SimpleBooleanProperty(false);

    private AccountBundle() {
        accounts = new ObservableListWrapper<>(new ArrayList<>());
        connectedAccounts = new ObservableListWrapper<>(new ArrayList<>());
        EventBus.subscribeToConnectionResponse(this);
    }

    public static ObservableList<AccountInfo> getAccounts() {
        return INSTANCE.accounts;
    }

    public static ObservableList<AccountInfo> getConnectedAccounts() {
        return INSTANCE.connectedAccounts;
    }

    public static void registerAccount(final String accountName, final BooleanProperty attached, final BooleanProperty connected) {
        Platform.runLater(() -> {
            HistoryBundle.createSpaceFor(accountName);
            final AccountInfo accountInfo = new AccountInfo(accountName, attached, connected);
            INSTANCE.accounts.add(accountInfo);
            if (connected.get()) {
                INSTANCE.connected.set(true);
                INSTANCE.connectedAccounts.add(accountInfo);
            }
//            connected.bind(accountInfo.connectedProperty().or());
            accountInfo.connectedProperty().addListener((ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) -> {
                if (newValue) {
                    INSTANCE.connectedAccounts.add(accountInfo);
                } else {
                    INSTANCE.connectedAccounts.remove(accountInfo);
                }
            });
        });
    }

    public static List<String> getAccountNames() {
        return getAccounts().stream().map(AccountInfo::getName).collect(Collectors.toList());
    }

    public static List<String> getConnectedAccountNames() {
        return getAccounts().stream().filter(AccountInfo::isConnected).map(AccountInfo::getName).collect(Collectors.toList());
    }

    public static List<String> getDetachedAccountNames() {
        return getAccounts().stream().filter(accountInfo ->
                !accountInfo.isAttached()).map(AccountInfo::getName).collect(Collectors.toList());
    }

    public Boolean isActive(final String accountName) {
        for (AccountInfo account : accounts) {
            if (account.getName().equals(accountName)) {
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

    @Override
    @Handler(rejectSubtypes = true)
    public void onEvent(final AccountConnectionResponse event) {
        final Optional<AccountInfo> matchingAccount = accounts.parallelStream().filter(accountInfo -> event.getAccount().equals(accountInfo.getName())).findAny();
        if (matchingAccount.isPresent()) {
            matchingAccount.get().setConnected(event.isConnected());
            accounts.stream().filter(AccountInfo::isConnected).forEach(account -> connected.set(true));
        }
    }
}
