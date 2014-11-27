package com.noe.hypercube.service;


import com.noe.hypercube.persistence.IAccountPersistenceController;
import com.noe.hypercube.persistence.domain.AccountEntity;
import com.noe.hypercube.persistence.domain.FileEntity;
import com.noe.hypercube.persistence.domain.MappingEntity;
import java.util.Optional;
import javafx.beans.property.SimpleBooleanProperty;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

public abstract class Client<ACCOUNT_TYPE extends Account, ENTITY_TYPE extends FileEntity, MAPPING_ENTITY extends MappingEntity> implements IClient<ACCOUNT_TYPE, ENTITY_TYPE, MAPPING_ENTITY> {

    @Inject
    private IAccountPersistenceController accountPersistenceController;

    private SimpleBooleanProperty connected = new SimpleBooleanProperty(false);
    private SimpleBooleanProperty attached = new SimpleBooleanProperty(false);

    private String refreshToken;
    private String accessToken;

    public Client() {
        final Optional<AccountEntity> storedAccountProperties = accountPersistenceController.findByAccountName(getAccountName());
        if (storedAccountProperties.isPresent()) {
            final AccountEntity accountEntity = storedAccountProperties.get();
            if (accountEntity.isAttached()) {
                attached.set(true);
                refreshToken = accountEntity.getRefreshToken();
                accessToken = accountEntity.getAccessToken();
            }
        }
    }

    @PostConstruct
    public void initState() {
        setConnected(testConnectionActive());
    }

    @Override
    public String getRefreshToken() {
        return refreshToken;
    }

    @Override
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Boolean isAttached() {
        return attached.get();
    }

    public SimpleBooleanProperty attachedProperty() {
        return attached;
    }

    public void setAttached(final Boolean attached) {
        this.attached.set(attached);
    }

    @Override
    public Boolean isConnected() {
        return connected.get();
    }

    public void setConnected(final Boolean connected) {
        this.connected.set(connected);
    }

    @Override
    public SimpleBooleanProperty connectedProperty() {
        return connected;
    }

    protected abstract boolean testConnectionActive();
}
