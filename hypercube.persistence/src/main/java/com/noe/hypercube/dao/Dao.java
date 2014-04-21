package com.noe.hypercube.dao;

import com.noe.hypercube.domain.IEntity;

import java.util.Collection;

public interface Dao<KEY, ENTITY_TYPE extends IEntity> {

    Class<ENTITY_TYPE> getEntityClass();

    void persist(ENTITY_TYPE entity);

    boolean remove(ENTITY_TYPE entity);

    boolean remove(KEY id);

    ENTITY_TYPE findById(KEY id);

    ENTITY_TYPE find(ENTITY_TYPE entity);

    public Collection<ENTITY_TYPE> getAll();
}
