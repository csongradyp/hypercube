package com.noe.hypercube.synchronization.upstream;

import com.noe.hypercube.mapping.DirectoryMapper;
import com.noe.hypercube.service.AccountType;
import com.noe.hypercube.synchronization.Action;
import com.noe.hypercube.synchronization.SynchronizationException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.noe.hypercube.synchronization.Action.*;

public class UpstreamSynchronizer implements IUpstreamSynchronizer {

    @Inject
    private Collection<DirectoryMapper> mappers;
    @Inject
    private Collection<Uploader> uploaders;
    private Map<Class<? extends AccountType>, Uploader> uploaderMap;

    public UpstreamSynchronizer() {
    }

    public UpstreamSynchronizer(final Collection<DirectoryMapper> mappers, final Collection<Uploader> uploaders) {
        this.mappers = mappers;
        this.uploaders = uploaders;
    }

    @PostConstruct
    private void createQueueMaps() {
        for (Uploader uploader : uploaders) {
            uploaderMap.put(uploader.getEntityType(), uploader);
        }
    }

    public void submitNew(final File file) throws SynchronizationException {
        submit(file, ADDED);
    }

    public void submitChanged(final File file) throws SynchronizationException {
        submit(file, CHANGED);
    }

    public void submitDelete(final File file) throws SynchronizationException {
        submit(file, REMOVED);
    }

    @Override
    public void submit(final File file, final Action action) throws SynchronizationException {
        for (DirectoryMapper mapper : mappers) {
            Path localPath = file.toPath();
            List<Path> remotePaths = mapper.getRemotes(localPath);
            if(isMapped(remotePaths)) {
                Uploader uploader = uploaderMap.get(mapper.getAccountType());
                for (Path remotePath : remotePaths) {
                    switch(action) {
                        case ADDED:
                            uploader.uploadNew(file, remotePath);
                            break;
                        case CHANGED:
                            uploader.uploadUpdated(file, remotePath);
                            break;
                        case REMOVED:
                            uploader.delete(file, remotePath);
                            break;
                    }
                }
            }
        }
    }

    private boolean isMapped(final List<Path> remotePaths) {
        return !remotePaths.isEmpty();
    }

}
