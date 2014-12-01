package com.noe.hypercube.controller;

import com.noe.hypercube.dao.IDao;
import com.noe.hypercube.persistence.domain.FileEntity;
import com.noe.hypercube.persistence.domain.IEntity;
import com.noe.hypercube.persistence.domain.LocalFileEntity;
import com.noe.hypercube.persistence.domain.MappingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Named
public class PersistenceController implements IPersistenceController {

    private static final Logger LOG = LoggerFactory.getLogger(PersistenceController.class);

    private final Map<Class<? extends IEntity>, IDao> daoMap;
    private final Collection<IDao<String, IEntity>> daos;
    @Inject
    private IDao<String, LocalFileEntity> localFileEntityDao;

    public PersistenceController(final Collection<IDao<String, IEntity>> daos) {
        daoMap = new HashMap<>();
        this.daos = daos;
    }

    public void createDaoMap() {
        for (IDao<String, IEntity> entityDao : daos) {
            daoMap.put(entityDao.getEntityClass(), entityDao);
        }
    }

    @Override
    public FileEntity get(final String id, final Class<? extends FileEntity> entityClass) {
        IDao<String, FileEntity> dao = daoMap.get(entityClass);
        return dao.findById(id);
    }

    @Override
    public FileEntity get(final FileEntity entity) {
        IDao<String, FileEntity> dao = daoMap.get(entity.getClass());
        return dao.findById(entity.getId());
    }

    @Override
    public Collection<FileEntity> getAll(final Class<? extends FileEntity> entityClass) {
        IDao dao = daoMap.get(entityClass);
        return dao.getAll();
    }

    @Override
    public void save(final FileEntity entity) {
        final Class<? extends FileEntity> entityClass = entity.getClass();
        IDao dao = daoMap.get(entityClass);
        dao.persist(entity);
    }

    @Override
    public void delete(final FileEntity entity) {
        final Class<? extends FileEntity> entityClass = entity.getClass();
        IDao dao = daoMap.get(entityClass);
        dao.remove(entity);
    }

    @Override
    public void delete(String id, Class<? extends FileEntity> entityClass) {
        IDao dao = daoMap.get(entityClass);
        dao.remove(id);
        LOG.debug("Successfully deleted {} from database", id);
    }

    @Override
    public Collection<MappingEntity> getMappings(Class<? extends MappingEntity> mappingClass) {
        final IDao dao = daoMap.get(mappingClass);
        return dao.getAll();
    }

    @Override
    public Collection<MappingEntity> getAllMappings() {
        final Collection<MappingEntity> allMappings = new ArrayList<>();
        final Collection<IDao> daos = daoMap.values();
        for (IDao dao : daos) {
            if (isMappingDao(dao)) {
                allMappings.addAll(dao.getAll());
            }
        }
        return allMappings;
    }

    @Override
    public void addMapping(MappingEntity mapping) {
        final Class<? extends MappingEntity> entityClass = mapping.getClass();
        final IDao dao = daoMap.get(entityClass);
        dao.persist(mapping);
        LOG.info("New mapping was persisted to as {}", dao.getEntityClass());
    }

    @Override
    public void removeMapping(final MappingEntity mapping) {
        final Class<? extends MappingEntity> entityClass = mapping.getClass();
        final IDao dao = daoMap.get(entityClass);
        dao.remove(mapping);
    }

    @Override
    public void removeMapping(final String id, final Class<? extends MappingEntity> mappingClass) {
        final IDao dao = daoMap.get(mappingClass);
        dao.remove(id);
    }

    @Override
    public Collection<Path> getMappedRemotes(Class<? extends MappingEntity> mappingClass) {
        final Collection<Path> mappedRemoteFolders = new ArrayList<>();
        final Collection<MappingEntity> mappings = getMappings(mappingClass);
        for (MappingEntity mapping : mappings) {
            mappedRemoteFolders.add(Paths.get(mapping.getRemoteDir()));
        }
        return mappedRemoteFolders;
    }

    @Override
    public Collection<Path> getRemoteFolder(final Class<? extends MappingEntity> mappingType, final Path targetFolder) {
        final Collection<Path> mappedRemoteFolders = new ArrayList<>();
        final Collection<MappingEntity> mappings = getMappings(mappingType);
        for (MappingEntity mapping : mappings) {
            final Path localPath = Paths.get(mapping.getLocalDir());
            if (targetFolder.equals(localPath)) {
                mappedRemoteFolders.add(Paths.get(mapping.getRemoteDir()));
            }
        }
        return mappedRemoteFolders;
    }

    @Override
    public Set<Class<IEntity>> getEntitiesMapping(final String folder) {
        final Set<Class<IEntity>> results = new HashSet<>();
        for (IDao<String, IEntity> dao : daos) {
            final IEntity entity = dao.findById(folder);
            if (entity != null) {
                results.add(dao.getEntityClass());
            }
        }
        return results;
    }

    @Override
    public Collection<String> getLocalMappings() {
        final Set<String> mappedLocalFolders = new HashSet<>();
        final Collection<IDao> daos = daoMap.values();
        for (IDao dao : daos) {
            if (isMappingDao(dao)) {
                final Collection<MappingEntity> daoMappings = dao.getAll();
                for (MappingEntity mapping : daoMappings) {
                    mappedLocalFolders.add(mapping.getLocalDir());
                }
            }
        }
        return mappedLocalFolders;
    }

    private boolean isMappingDao(IDao dao) {
        return MappingEntity.class.isAssignableFrom(dao.getEntityClass());
    }

    @Override
    public Map<String, List<FileEntity>> getMappedEntities(final String folder) {
        final Map<String, List<FileEntity>> fileEntities = new HashMap<>();
        final Collection<IDao> daos = daoMap.values();
        for (IDao dao : daos) {
            if (isFileEntityDao(dao)) {
                final Collection<? extends FileEntity> mappings = dao.getAll();
                for (FileEntity fileEntity : mappings) {
                    final String localPath = fileEntity.getLocalPath();
                    if (localPath.startsWith(folder)) {
                        if (!fileEntities.containsKey(localPath)) {
                            fileEntities.put(localPath, new ArrayList<>());
                        }
                        fileEntities.get(localPath).add(fileEntity);
                    }
                }
            }
        }
        return fileEntities;
    }

    @Override
    public List<MappingEntity> getMappings(final String folder) {
        final List<MappingEntity> allMappings = new ArrayList<>();
        final Collection<IDao> daos = daoMap.values();
        for (IDao dao : daos) {
            if (isMappingDao(dao)) {
                final Collection<MappingEntity> all = dao.getAll();
                for (MappingEntity mapping : all) {
                    if (mapping.getLocalDir().equals(folder)) {
                        allMappings.add(mapping);
                    }
                }
            }
        }
        return allMappings;
    }

    private boolean isFileEntityDao(final IDao dao) {
        return FileEntity.class.isAssignableFrom(dao.getEntityClass());
    }

    @Override
    public void save(LocalFileEntity localFileEntity) {
        localFileEntityDao.persist(localFileEntity);
    }

    @Override
    public LocalFileEntity getLocalFileEntity(final Path localFilePath) {
        return localFileEntityDao.findById(localFilePath.toString());
    }
}
