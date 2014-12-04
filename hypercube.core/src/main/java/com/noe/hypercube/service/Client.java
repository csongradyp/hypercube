package com.noe.hypercube.service;


import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.EventHandler;
import com.noe.hypercube.event.domain.request.AccountConnectionRequest;
import com.noe.hypercube.persistence.IAccountPersistenceController;
import com.noe.hypercube.persistence.domain.AccountEntity;
import com.noe.hypercube.persistence.domain.FileEntity;
import com.noe.hypercube.persistence.domain.MappingEntity;
import java.util.Optional;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import net.engio.mbassy.listener.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Client<ACCOUNT_TYPE extends Account, CLIENT, ENTITY_TYPE extends FileEntity, MAPPING_ENTITY extends MappingEntity> implements IClient<ACCOUNT_TYPE, ENTITY_TYPE, MAPPING_ENTITY>, EventHandler<AccountConnectionRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(Client.class);

    private SimpleBooleanProperty connected = new SimpleBooleanProperty(false);
    private SimpleBooleanProperty attached = new SimpleBooleanProperty(false);
    @Inject
    private IAccountPersistenceController accountPersistenceController;
    private Authentication<CLIENT> authentication;
    private CLIENT client;

    public Client(final Authentication<CLIENT> authentication) {
        this.authentication = authentication;
    }

    @PostConstruct
    public void initState() {
        final Optional<AccountEntity> storedAccountProperties = accountPersistenceController.findByAccountName(getAccountName());
        if (storedAccountProperties.isPresent()) {
            final AccountEntity accountEntity = storedAccountProperties.get();
            attached.set(accountEntity.isAttached());
            if (accountEntity.isAttached()) {
                client = authentication.getClient(accountEntity.getRefreshToken(), accountEntity.getAccessToken());
            }
            setConnected(testConnection());
        } else {
            final AccountEntity accountEntity = new AccountEntity(getAccountName());
            accountEntity.setAttached(false);
            accountPersistenceController.save(accountEntity);
        }
        disconnectWhenDetached();
        EventBus.subscribeToConnectionRequest(this);

    }

    private void disconnectWhenDetached() {
        attached.addListener((ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) -> {
            if(!newValue) {
                connected.set(newValue);
            }
        });
    }

    private CLIENT createClientWithNewAuthentication() {
        try {
            return authentication.createClient();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException("Client could not be created", e);
        }
    }

    protected CLIENT getClient() {
        return client;
    }

    public Authentication<CLIENT> getAuthentication() {
        return authentication;
    }

    protected void storeNewTokens(final String refreshToken, final String accessToken) {
        authentication.storeTokens(refreshToken, accessToken);
    }

    @Override
    public Boolean isAttached() {
        return attached.get();
    }

    @Override
    public SimpleBooleanProperty attachedProperty() {
        return attached;
    }

    @Override
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

    protected abstract boolean testConnection();

    @Override
    @Handler(rejectSubtypes = true)
    public void onEvent(final AccountConnectionRequest event) {
        if(event.getAccount().equals(getAccountName()) && !isAttached()) {
            client = createClientWithNewAuthentication();
        }
    }
}
