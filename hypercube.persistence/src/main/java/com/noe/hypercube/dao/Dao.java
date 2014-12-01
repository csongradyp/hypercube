package com.noe.hypercube.dao;

import com.noe.hypercube.persistence.domain.IEntity;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.inject.Inject;
import java.util.Collection;

public abstract class Dao<ENTITY extends IEntity> implements IDao<String, ENTITY> {

    @Inject
    @Lazy
    private JpaRepository<ENTITY, String> repository;

    protected Dao() {
    }

    public abstract Class<ENTITY> getEntityClass();

    @Override
    public void persist(final ENTITY entity) {
        repository.saveAndFlush(entity);
    }

    @Override
    public void remove(final ENTITY entity) {
        repository.delete(entity);
    }

    @Override
    public void remove(final String id) {
        repository.delete(id);
    }

    @Override
    public ENTITY findById(final String id) {
        return repository.findOne(id);
    }

    @Override
    public Collection<ENTITY> getAll() {
        return repository.findAll();
    }
}
