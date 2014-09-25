package com.noe.hypercube.controller;

import com.noe.hypercube.dao.Dao;
import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.domain.IEntity;
import com.noe.hypercube.domain.MappingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.util.*;

@Named
public class PersistenceController implements IPersistenceController {

    private static final Logger LOG = LoggerFactory.getLogger(PersistenceController.class);

    private final Map<Class<? extends IEntity>, Dao> daoMap;
    private final Collection<Dao<String, IEntity>> daos;

    public PersistenceController(final Collection<Dao<String, IEntity>> daos) {
        daoMap = new HashMap<>();
        this.daos = daos;
    }

    public void createDaoMap() {
        for (Dao<String, IEntity> entityDao : daos) {
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
        if (deleted) {
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
        Collection<Dao> daos = daoMap.values();
        for (Dao dao : daos) {
            if (isMappingDao(dao)) {
                allMappings.addAll(dao.getAll());
            }
        }
        return allMappings;
    }

    @Override
    public void addMapping(MappingEntity mapping) {
        Class<? extends MappingEntity> entityClass = mapping.getClass();
        Dao dao = daoMap.get(entityClass);
        dao.persist(mapping);
    }

    @Override
    public void removeMapping(MappingEntity mapping) {
        Class<? extends MappingEntity> entityClass = mapping.getClass();
        Dao dao = daoMap.get(entityClass);
        dao.remove(mapping);
    }

    @Override
    public Set<Class<IEntity>> getEntitiesMapping(String folder) {
        Set<Class<IEntity>> results = new HashSet<>();
        for (Dao<String, IEntity> dao : daos) {
            final IEntity entity = dao.findById(folder);
            if(entity != null) {
                results.add(dao.getEntityClass());
            }
        }
        return results;
    }

    @Override
    public Collection<String> getLocalMappings() {
        Set<String> mappedLocalFolders = new HashSet<>();
        Collection<Dao> daos = daoMap.values();
        for (Dao dao : daos) {
            if(isMappingDao(dao)) {
                final Collection<MappingEntity> daoMappings = dao.getAll();
                for (MappingEntity mapping : daoMappings) {
                    mappedLocalFolders.add(mapping.getLocalDir());
                }
            }
        }
        return mappedLocalFolders;
    }

    private boolean isMappingDao(Dao dao) {
        return MappingEntity.class.isAssignableFrom(dao.getEntityClass());
    }

    @Override
    public Map<String, List<FileEntity>> getMappedEntities(final String folder){
        final Map<String, List<FileEntity>> fileEntities = new HashMap<>();
        final Collection<Dao> daos = daoMap.values();
        for (Dao dao : daos) {
            if(isFileEntityDao(dao)) {
                final Collection<? extends FileEntity> mappings = dao.getAll();
                for (FileEntity fileEntity : mappings) {
                    final String localPath = fileEntity.getLocalPath();
                    if(localPath.startsWith(folder)) {
                        if(!fileEntities.containsKey(localPath)) {
                            fileEntities.put(localPath, new ArrayList<>());
                        }
                        fileEntities.get(localPath).add(fileEntity);
                    }
                }
            }
        }
        return fileEntities;
    }

    private boolean isFileEntityDao(Dao dao) {
        return FileEntity.class.isAssignableFrom(dao.getEntityClass());
    }
}
