package com.noe.hypercube.dao;

import com.noe.hypercube.persistence.domain.IEntity;

import java.util.Collection;

public interface IDao<KEY, ENTITY_TYPE extends IEntity> {

    Class<ENTITY_TYPE> getEntityClass();

    void persist(ENTITY_TYPE entity);

    void remove(ENTITY_TYPE entity);

    void remove(KEY id);

    ENTITY_TYPE findById(KEY id);

    public Collection<ENTITY_TYPE> getAll();
}
