package com.noe.hypercube.service;

import com.noe.hypercube.persistence.domain.AccountEntity;
import java.util.Optional;

public interface IAuthentication {

    String getAccountName();

    void storeTokens(String refreshToken, String accessToken);

    Optional<AccountEntity> getStoredAccountProperties();
}
