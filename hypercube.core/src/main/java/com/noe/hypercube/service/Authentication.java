package com.noe.hypercube.service;

import com.noe.hypercube.persistence.IAccountPersistenceController;
import com.noe.hypercube.persistence.domain.AccountEntity;
import java.util.Optional;
import javax.inject.Inject;

public abstract class Authentication<CLIENT> implements IAuthentication {

    @Inject
    IAccountPersistenceController accountPersistenceController;

    @Override
    public void storeTokens(final String refreshToken, final String accessToken) {
        final Optional<AccountEntity> account = accountPersistenceController.findByAccountName(getAccountName());
        final AccountEntity accountEntity;
        if (account.isPresent()) {
            accountEntity = account.get();
        }
        else {
            accountEntity = new AccountEntity(getAccountName());
            accountEntity.setAttached(true);
        }
        accountEntity.setRefreshToken(refreshToken);
        accountEntity.setAccessToken(accessToken);
        accountPersistenceController.save(accountEntity);
    }

    @Override
    public Optional<AccountEntity> getStoredAccountProperties() {
        return accountPersistenceController.findByAccountName(getAccountName());
    }

    public abstract CLIENT createClient() throws Exception;

    public abstract CLIENT getClient(final String refreshToken, final String accessToken);
}
