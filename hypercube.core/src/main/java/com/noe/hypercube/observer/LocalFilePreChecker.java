package com.noe.hypercube.observer;

import com.noe.hypercube.controller.EntityController;
import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.mapping.DirectoryMapper;
import com.noe.hypercube.synchronization.SynchronizationException;
import com.noe.hypercube.synchronization.presynchronization.FilePreSynchronizer;
import com.noe.hypercube.synchronization.upstream.IUploader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class LocalFilePreChecker implements FilePreSynchronizer {

    private static final Logger LOG = LoggerFactory.getLogger(LocalFilePreChecker.class);

    private IUploader upstreamSynchronizer;
    private DirectoryMapper directoryMapper;
    private EntityController entityController;

    protected LocalFilePreChecker(IUploader upstreamSynchronizer, DirectoryMapper directoryMapper) {
        this.upstreamSynchronizer = upstreamSynchronizer;
        this.directoryMapper = directoryMapper;
    }

    protected abstract Collection<? extends FileEntity> getMappedFiles();

    public void run(File[] currentLocalFiles)  {
        Collection<? extends FileEntity> databaseEntries = getMappedFiles();
        try {
        Map<Path, FileEntity> dbEntryMap = locateChangedLocalFiles(currentLocalFiles, toMap(databaseEntries));
            for (Path deletedLocalFile : dbEntryMap.keySet()) {
                List<Path> remotePaths = directoryMapper.getRemotes(deletedLocalFile);
                for (Path remotePath : remotePaths) {
                    upstreamSynchronizer.delete(deletedLocalFile, remotePath);
                }
            }
        } catch (SynchronizationException e) {
            LOG.error("Error during deleting remote content @ Pre-Synchronization state", e);
        }
    }

    private Map<Path, FileEntity> toMap(Collection<? extends FileEntity> list) {
        Map<Path, FileEntity> map = new HashMap<>();
        for (FileEntity element : list) {
            map.put(Paths.get(element.getLocalPath()), element);
        }
        return map;
    }

    private Map<Path, FileEntity> locateChangedLocalFiles(File[] currentLocalFiles, Map<Path, FileEntity> databaseEntryMap) throws SynchronizationException {
        for (File localFile : currentLocalFiles) {
            if(!localFile.isDirectory()) {
                Path filePath = Paths.get(localFile.toURI());

                if(databaseEntryMap.containsKey(filePath)) {
                    Date date = new Date(localFile.lastModified());
                    FileEntity dbEntry = databaseEntryMap.get(filePath);
                    if(date.after(dbEntry.lastModified())) {
                        LOG.debug("New local file has been updated before start: " + filePath);
                        upstreamSynchronizer.uploadUpdated(localFile);
                    }
                    else{
                        LOG.debug("Untouched local file: " + filePath);
                    }
                    databaseEntryMap.remove(filePath);
                }
                else {
                    LOG.debug("New local file found: {}", filePath);
                    upstreamSynchronizer.uploadNew(filePath);
                }
            }
            else {
                locateChangedLocalFiles(localFile.listFiles(), databaseEntryMap);
            }
        }
        return databaseEntryMap;
    }
}
