package com.noe.hypercube.observer;

import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.domain.MappingEntity;
import com.noe.hypercube.mapping.DirectoryMapper;
import com.noe.hypercube.service.AccountType;
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
    private IPersistenceController controller;
    @Inject
    private Collection<? extends IUploader> uploaders;
    @Inject
    private Collection<DirectoryMapper> directoryMappers;

    public ObserverFactory(List<? extends IUploader> uploaders, Collection<DirectoryMapper> directoryMappers) {
        this.uploaders = uploaders;
        this.directoryMappers = directoryMappers;
    }

    public List<FileAlterationObserver> create() {
//        controller.save(new AMappingEntity("C:\\temp", "/x/y"));
        List<FileAlterationObserver> observers = new LinkedList<>();
        Collection<MappingEntity> mappings = controller.getAllMappings();
        if(mappings != null) {
            for (MappingEntity entity : mappings) {
                FileAlterationObserver observer = createObserver(entity, uploaders);
                observers.add(observer);
            }
        }
        return observers;
    }

    private FileAlterationObserver createObserver(MappingEntity entity, Collection<? extends IUploader> uploaders) {
        Class<? extends AccountType> accountType = entity.getAccountType();
        Map<Class<? extends AccountType>, ? extends IUploader> uploaderMap = getUploaderMap(uploaders);
        Map<Class<? extends AccountType>, ? extends DirectoryMapper> mapperMap = getMapperMap(directoryMappers);

        IUploader uploader = uploaderMap.get(accountType);
        DirectoryMapper mapper = mapperMap.get(accountType);
        validate(accountType, uploader, mapper);

        Path localDir = Paths.get(entity.getLocalDir());
        LOG.info("File observer created for {} in '{}' directoy ", accountType.getSimpleName(), localDir);
        return new LocalFileObserver(localDir, new LocalFileListener(uploader, mapper));
    }

    private void validate(Class<? extends AccountType> accountType, IUploader uploader, DirectoryMapper mapper) {
        if(uploader == null || mapper == null) {
            throw new IllegalStateException("Observer creation failed for " + accountType.getSimpleName());
        }
    }

    private Map<Class<? extends AccountType>, IUploader> getUploaderMap(Collection<? extends IUploader> uploaders) {
        Map<Class<? extends AccountType>, IUploader> uploaderMap = new LinkedHashMap<>();
        for (IUploader uploader : uploaders) {
            uploaderMap.put(uploader.getAccountType(), uploader);
        }
        return uploaderMap;
    }

    private Map<Class<? extends AccountType>, ? extends DirectoryMapper> getMapperMap(Collection<DirectoryMapper> directoryMappers) {
        Map<Class<? extends AccountType>, DirectoryMapper> uploaderMap = new LinkedHashMap<>();
        for (DirectoryMapper mapper : directoryMappers) {
            uploaderMap.put(mapper.getAccountType(), mapper);
        }
        return uploaderMap;
    }
}
