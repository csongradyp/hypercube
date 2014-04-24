package com.noe.hypercube.synchronization.upstream;

import com.noe.hypercube.mapping.DirectoryMapper;
import com.noe.hypercube.service.AccountType;
import com.noe.hypercube.synchronization.Action;
import com.noe.hypercube.synchronization.SynchronizationException;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.noe.hypercube.synchronization.Action.*;

public class UpstreamSynchronizer implements IUpstreamSynchronizer {

    private final Collection<DirectoryMapper> mapperCollection;
    private final Collection<Uploader> uploaderCollection;
    private Map<Class<? extends AccountType>, Uploader> uploaders;

    public UpstreamSynchronizer(Collection<DirectoryMapper> mapperCollection, Collection<Uploader> uploaderCollection) {
        this.mapperCollection = mapperCollection;
        this.uploaderCollection = uploaderCollection;
    }

    @PostConstruct
    private void createQueueMaps() {
        for (Uploader uploader : uploaderCollection) {
            uploaders.put(uploader.getEntityClass(), uploader);
        }
    }

    public void submitNew(File file) throws SynchronizationException {
        submit(file, ADDED);
    }

    public void submitChanged(File file) throws SynchronizationException {
        submit(file, CHANGED);
    }

    public void submitDelete(File file) throws SynchronizationException {
        submit(file, REMOVED);
    }

    @Override
    public void submit(File file, Action action) throws SynchronizationException {
        for (DirectoryMapper mapper : mapperCollection) {
            Path localPath = file.toPath();
            List<Path> remotePaths = mapper.getRemotes(localPath);
            if(isMapped(remotePaths)) {
                Uploader uploader = uploaders.get(mapper.getAccountType());
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

    private boolean isMapped(List<Path> remotePaths) {
        return !remotePaths.isEmpty();
    }

}
