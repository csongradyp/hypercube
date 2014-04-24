package com.noe.hypercube.synchronization.presynchronization;

import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.observer.LocalFileListener;
import com.noe.hypercube.service.AccountType;
import com.noe.hypercube.synchronization.SynchronizationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.io.FileUtils.isFileNewer;

public abstract class LocalFilePreChecker<ACCOUNT_TYPE extends AccountType> implements FilePreSynchronizer {

    private static final Logger LOG = LoggerFactory.getLogger(LocalFilePreChecker.class);

    private LocalFileListener<ACCOUNT_TYPE> fileListener;

    protected LocalFilePreChecker(LocalFileListener<ACCOUNT_TYPE> fileListener) {
        this.fileListener = fileListener;
    }

    protected abstract Collection<? extends FileEntity> getMappedFiles();

    @Override
    public void run(Collection<File> currentLocalFiles)  {
        Collection<? extends FileEntity> databaseEntries = getMappedFiles();
        try {
            Map<Path, FileEntity> dbEntryMap = uploadChanged(currentLocalFiles, toMap(databaseEntries));
            deleteUnexistingFiles(dbEntryMap);
        } catch (SynchronizationException e) {
            LOG.error("Error @ Pre-Synchronization state", e);
        }
    }

    private void deleteUnexistingFiles(Map<Path, FileEntity> dbEntryMap) {
        for (Path deletedLocalFile : dbEntryMap.keySet()) {
            fileListener.onFileDelete(deletedLocalFile.toFile());
        }
    }

    private Map<Path, FileEntity> toMap(Collection<? extends FileEntity> list) {
        Map<Path, FileEntity> map = new HashMap<>();
        for (FileEntity element : list) {
            map.put(Paths.get(element.getLocalPath()), element);
        }
        return map;
    }

    private Map<Path, FileEntity> uploadChanged(Collection<File> currentLocalFiles, Map<Path, FileEntity> mappedLocalFiles) throws SynchronizationException {
        for (File localFile : currentLocalFiles) {

            if(!localFile.isDirectory()) {
                Path localFilePath = localFile.toPath();

                if(isMapped(localFilePath, mappedLocalFiles)) {
                    FileEntity dbEntry = mappedLocalFiles.get(localFilePath);
                    uploadIfChanged(localFile, dbEntry);
                    mappedLocalFiles.remove(localFilePath);
                }
                else {
                    LOG.debug("New local file found: {}", localFilePath);
                    fileListener.onFileCreate(localFile);
                }
            }
            else {
                uploadChanged(FileUtils.listFiles(localFile, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE), mappedLocalFiles);
            }
        }
        return mappedLocalFiles;
    }

    private boolean isMapped(Path localFilePath, Map<Path, FileEntity> mappedLocalFiles) {
        return mappedLocalFiles.containsKey(localFilePath);
    }

    private void uploadIfChanged(File localFile, FileEntity dbEntry) {
        Path localFilePath = localFile.toPath();
        if(isFileNewer(localFile, dbEntry.lastModified())) {
            LOG.debug("New local file has been updated before start: " + localFilePath);
            fileListener.onFileChange(localFile);
        }
        else{
            LOG.debug("Untouched local file: " + localFilePath);
        }
    }
}
