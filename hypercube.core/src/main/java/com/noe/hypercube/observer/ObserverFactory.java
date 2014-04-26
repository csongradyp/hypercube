package com.noe.hypercube.observer;

import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.domain.MappingEntity;
import com.noe.hypercube.mapping.DirectoryMapper;
import com.noe.hypercube.service.Account;
import com.noe.hypercube.synchronization.presynchronization.LocalFilePreSynchronizer;
import com.noe.hypercube.synchronization.upstream.IUploader;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Named
public class ObserverFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ObserverFactory.class);

    @Inject
    private IPersistenceController persistenceController;
    private final Collection<? extends IUploader> uploaders;
    private final Collection<DirectoryMapper> mappers;

    public ObserverFactory(List<? extends IUploader> uploaders, Collection<DirectoryMapper> mappers) {
        this.uploaders = uploaders;
        this.mappers = mappers;
    }

    public List<FileAlterationObserver> create() {
        List<FileAlterationObserver> observers = new LinkedList<>();
        final Map<Class<? extends Account>, ? extends IUploader> uploaderMap = createUploaderMap(uploaders);
        final Map<Class<? extends Account>, ? extends DirectoryMapper> mapperMap = createMapperMap(mappers);
        final Collection<MappingEntity> mappings = persistenceController.getAllMappings();
        if(mappings != null) {
            for (MappingEntity entity : mappings) {
                FileAlterationObserver observer = createObserver(entity, uploaderMap, mapperMap);
                LOG.info("File observer created for {} to watch '{}' directoy ", entity.getAccountType(), entity.getLocalDir());
                observers.add(observer);
            }
        }
        return observers;
    }

    private FileAlterationObserver createObserver(final MappingEntity entity, final Map<Class<? extends Account>, ? extends IUploader> uploaderMap,  final Map<Class<? extends Account>, ? extends DirectoryMapper> mapperMap) {
        final Class<? extends Account> accountType = entity.getAccountType();
        final IUploader uploader = uploaderMap.get(accountType);
        final DirectoryMapper mapper = mapperMap.get(accountType);
        validate(accountType, uploader, mapper);

        final Path localDir = Paths.get(entity.getLocalDir());
        final LocalFileListener listener = new LocalFileListener(uploader, mapper);
        final LocalFilePreSynchronizer preSynchronizer = new LocalFilePreSynchronizer(listener, persistenceController.getAll(uploader.getEntityType()));
        return new LocalFileObserver(localDir, listener, preSynchronizer);
    }

    private void validate(Class<? extends Account> accountType, IUploader uploader, DirectoryMapper mapper) {
        if(uploader == null || mapper == null) {
            throw new IllegalStateException("Observer creation failed for " + accountType.getSimpleName());
        }
    }

    private Map<Class<? extends Account>, IUploader> createUploaderMap(final Collection<? extends IUploader> uploaders) {
        Map<Class<? extends Account>, IUploader> uploaderMap = new LinkedHashMap<>();
        for (IUploader uploader : uploaders) {
            uploaderMap.put(uploader.getAccountType(), uploader);
        }
        return uploaderMap;
    }

    private Map<Class<? extends Account>, ? extends DirectoryMapper> createMapperMap(final Collection<DirectoryMapper> directoryMappers) {
        Map<Class<? extends Account>, DirectoryMapper> uploaderMap = new LinkedHashMap<>();
        for (DirectoryMapper mapper : directoryMappers) {
            uploaderMap.put(mapper.getAccountType(), mapper);
        }
        return uploaderMap;
    }
}
