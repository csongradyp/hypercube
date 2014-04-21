package com.noe.hypercube.controller;

import com.noe.hypercube.dao.Dao;
import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.domain.IEntity;
import com.noe.hypercube.domain.MappingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@Named
public class PersistenceController implements IPersistenceController {

    private static final Logger LOG = LoggerFactory.getLogger(PersistenceController.class);

    private final Map<Class<? extends IEntity>, Dao> daoMap;
    @Inject
    private Collection<Dao<String, IEntity>> daoCollection;

    public PersistenceController() {
        daoMap = new HashMap<>();
    }

    public PersistenceController(final Collection<Dao<String, IEntity>> daoCollection) {
        this();
        this.daoCollection = daoCollection;
        createDaoMap();
    }

    @PostConstruct
    private void createDaoMap() {
        for (Dao<String, IEntity> entityDao : daoCollection) {
            daoMap.put(entityDao.getEntityClass(), entityDao);
        }
    }

    @Override
    public FileEntity get(String id, Class<? extends FileEntity> entityClass) {
        Dao<String, FileEntity> dao = daoMap.get(entityClass);
        return dao.findById(id);
    }

    @Override
    public FileEntity get(FileEntity entity) {
        Dao<String, FileEntity> dao = daoMap.get(entity.getClass());
        return dao.findById(entity.getId());
    }

    @Override
    public Collection<FileEntity> getAll(Class<? extends FileEntity> entityClass) {
        Dao dao = daoMap.get(entityClass);
        return dao.getAll();
    }

    @Override
    public void save(FileEntity entity) {
        Class<? extends FileEntity> entityClass = entity.getClass();
        Dao dao = daoMap.get(entityClass);
        dao.persist(entity);
    }

    @Override
    public void delete(FileEntity entity) {
        Class<? extends FileEntity> entityClass = entity.getClass();
        Dao dao = daoMap.get(entityClass);
        dao.remove(entity);
    }

    @Override
    public boolean delete(String id, Class<? extends FileEntity> entityClass) {
        Dao dao = daoMap.get(entityClass);
        boolean deleted = dao.remove(id);
        if(deleted) {
            LOG.debug("Successfully deleted {} from database", id);
        }
        return deleted;
    }

    @Override
    public Collection<MappingEntity> getMappings(Class<? extends MappingEntity> mappingClass) {
        Dao dao = daoMap.get(mappingClass);
        return dao.getAll();
    }

    @Override
    public Collection<MappingEntity> getAllMappings() {
        Collection<MappingEntity> allMappings = new LinkedList<>();
        for (Dao dao : daoMap.values()) {
            allMappings.addAll(dao.getAll());
        }
        return allMappings;
    }
}
