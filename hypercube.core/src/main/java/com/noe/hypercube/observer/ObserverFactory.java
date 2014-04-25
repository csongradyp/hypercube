package com.noe.hypercube.observer;

import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.domain.MappingEntity;
import com.noe.hypercube.mapping.DirectoryMapper;
import com.noe.hypercube.service.AccountType;
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
    @Inject
    private Collection<? extends IUploader> uploaders;
    @Inject
    private Collection<DirectoryMapper> directoryMappers;

    public ObserverFactory(List<? extends IUploader> uploaders, Collection<DirectoryMapper> directoryMappers) {
        this.uploaders = uploaders;
        this.directoryMappers = directoryMappers;
    }

    public List<FileAlterationObserver> create() {
        List<FileAlterationObserver> observers = new LinkedList<>();
        final Map<Class<? extends AccountType>, ? extends IUploader> uploaderMap = createUploaderMap(uploaders);
        final Map<Class<? extends AccountType>, ? extends DirectoryMapper> mapperMap = createMapperMap(directoryMappers);
        final Collection<MappingEntity> mappings = persistenceController.getAllMappings();
        if(mappings != null) {
            for (MappingEntity entity : mappings) {
                FileAlterationObserver observer = createObserver(entity, uploaderMap, mapperMap);
                observers.add(observer);
            }
        }
        return observers;
    }

    private FileAlterationObserver createObserver(MappingEntity entity, final Map<Class<? extends AccountType>, ? extends IUploader> uploaderMap,  final Map<Class<? extends AccountType>, ? extends DirectoryMapper> mapperMap) {
        final Class<? extends AccountType> accountType = entity.getAccountType();
        final IUploader uploader = uploaderMap.get(accountType);
        final DirectoryMapper mapper = mapperMap.get(accountType);
        validate(accountType, uploader, mapper);

        final Path localDir = Paths.get(entity.getLocalDir());
        LOG.info("File observer created for {} in '{}' directoy ", accountType.getSimpleName(), localDir);
        final LocalFileListener listener = new LocalFileListener(uploader, mapper);
        final LocalFilePreSynchronizer preSynchronizer = new LocalFilePreSynchronizer(listener, persistenceController.getAll(uploader.getEntityType()));
        return new LocalFileObserver(localDir, listener, preSynchronizer);
    }

    private void validate(Class<? extends AccountType> accountType, IUploader uploader, DirectoryMapper mapper) {
        if(uploader == null || mapper == null) {
            throw new IllegalStateException("Observer creation failed for " + accountType.getSimpleName());
        }
    }

    private Map<Class<? extends AccountType>, IUploader> createUploaderMap(Collection<? extends IUploader> uploaders) {
        Map<Class<? extends AccountType>, IUploader> uploaderMap = new LinkedHashMap<>();
        for (IUploader uploader : uploaders) {
            uploaderMap.put(uploader.getAccountType(), uploader);
        }
        return uploaderMap;
    }

    private Map<Class<? extends AccountType>, ? extends DirectoryMapper> createMapperMap(Collection<DirectoryMapper> directoryMappers) {
        Map<Class<? extends AccountType>, DirectoryMapper> uploaderMap = new LinkedHashMap<>();
        for (DirectoryMapper mapper : directoryMappers) {
            uploaderMap.put(mapper.getAccountType(), mapper);
        }
        return uploaderMap;
    }
}
