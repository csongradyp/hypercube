package com.noe.hypercube.dao;

import com.noe.hypercube.domain.IEntity;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Collection;

@Repository
public abstract class EntityDao<ENTITY_TYPE extends IEntity> implements Dao<String, ENTITY_TYPE> {

    private static final Logger LOG = Logger.getLogger(EntityDao.class);

    @PersistenceContext(unitName = "hyperPersistenceUnit")
    protected EntityManager entityManager;

    @Override
    public abstract Class<ENTITY_TYPE> getEntityClass();

    @Override
    @Transactional
    public void persist(ENTITY_TYPE entity) {
        if (entityManager.contains(entity)) {
            entityManager.merge(entity);
        } else {
            entityManager.persist(entity);
        }
    }

    @Override
    @Transactional
    public boolean remove(ENTITY_TYPE entity) {
        ENTITY_TYPE managedEntity = entityManager.merge(entity);
        try {
            entityManager.remove(managedEntity);
        } catch (IllegalStateException e) {
            LOG.error("Failed to delete entity: " + entity, e);
            return false;
        }
        return true;
    }

    @Override
    @Transactional
    public boolean remove(String id) {
        ENTITY_TYPE entity = findById(id);
        return remove(entity);
    }

    @Override
    public ENTITY_TYPE find(ENTITY_TYPE entity) {
        try {
        return entityManager.find(getEntityClass(), entity);
        } catch (Exception e) {
            LOG.error(e);
        }
        return null;
    }

    @Override
    public ENTITY_TYPE findById(String id) {
        try {
            return entityManager.find(getEntityClass(), id);
        } catch (Exception e) {
            LOG.error(e);
        }
        return null;
    }

    @Override
    public Collection<ENTITY_TYPE> getAll() {
        try {
            Query query = entityManager.createQuery("SELECT e FROM " + getEntityClass().getSimpleName() + " e");
            return (Collection<ENTITY_TYPE>) query.getResultList();
        } catch (Exception e) {
            LOG.error(e);
        }
        return new ArrayList<>();
    }

}
