package com.noe.hypercube.dao;

import com.noe.hypercube.domain.IEntity;
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

    public void persist(ENTITY entity) {
        repository.saveAndFlush(entity);
    }

    public void remove(ENTITY entity) {
        repository.delete(entity);
    }

    public void remove(String id) {
        repository.delete(id);
    }

    public ENTITY findById(String id) {
        return repository.findOne(id);
    }

    public Collection<ENTITY> getAll() {
        return repository.findAll();
    }
}
