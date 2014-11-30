package com.noe.hypercube.service;


import com.noe.hypercube.persistence.domain.AccountEntity;
import com.noe.hypercube.persistence.domain.FileEntity;
import com.noe.hypercube.persistence.domain.MappingEntity;
import java.util.Optional;
import javafx.beans.property.SimpleBooleanProperty;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Client<ACCOUNT_TYPE extends Account, CLIENT, ENTITY_TYPE extends FileEntity, MAPPING_ENTITY extends MappingEntity> implements IClient<ACCOUNT_TYPE, ENTITY_TYPE, MAPPING_ENTITY> {

    private static final Logger LOG = LoggerFactory.getLogger(Client.class);

    private SimpleBooleanProperty connected = new SimpleBooleanProperty(false);
    private SimpleBooleanProperty attached = new SimpleBooleanProperty(false);
    protected Authentication<CLIENT> authentication;
    private CLIENT client;

    public Client(Authentication<CLIENT> authentication) {
        this.authentication = authentication;
    }

    @PostConstruct
    public void initState() {
        final Optional<AccountEntity> storedAccountProperties = authentication.getStoredTokens();
        if (storedAccountProperties.isPresent()) {
            final AccountEntity accountEntity = storedAccountProperties.get();
            if (accountEntity.isAttached()) {
                attached.set(true);
                client = createClient(accountEntity.getRefreshToken(), accountEntity.getAccessToken());
            }
        } else {
            client = createClientWithNewAuthentication();
        }
        setConnected(testConnectionActive());
    }

    protected CLIENT createClientWithNewAuthentication() {
        try {
            return authentication.createClient();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException("Client could not be created", e);
        }
    }

    protected abstract CLIENT createClient(final String refreshToken, final String accessToken);

    protected CLIENT getClient() {
        return client;
    }

    public Authentication<CLIENT> getAuthentication() {
        return authentication;
    }

    protected void storeNewTokens(final String refreshToken, final String accessToken) {
        authentication.storeTokens(refreshToken, accessToken);
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
