package com.noe.hypercube.service;


import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.domain.MappingEntity;
import javafx.beans.property.SimpleBooleanProperty;

import javax.annotation.PostConstruct;

public abstract class Client<ACCOUNT_TYPE extends Account, ENTITY_TYPE extends FileEntity, MAPPING_ENTITY extends MappingEntity> implements IClient<ACCOUNT_TYPE, ENTITY_TYPE, MAPPING_ENTITY> {

    private SimpleBooleanProperty connected = new SimpleBooleanProperty(false);

    @Override
    public Boolean isConnected() {
        return connected.get();
    }

    public void setConnected(Boolean connected) {
        this.connected.set(connected);
    }

    @Override
    public SimpleBooleanProperty connectedProperty() {
        return connected;
    }

    @PostConstruct
    public void initState() {
        connected.set(testConnectionActive());
    }

    protected abstract boolean testConnectionActive();
}
