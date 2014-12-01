package com.noe.hypercube.synchronization.presynchronization;

import com.noe.hypercube.persistence.domain.FileEntity;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.io.FileUtils.isFileNewer;

public class LocalFilePreSynchronizer implements IFilePreSynchronizer {

    private static final Logger LOG = LoggerFactory.getLogger(LocalFilePreSynchronizer.class);

    private final FileAlterationListener fileListener;
    private final Collection<? extends FileEntity> mappedFiles;

    public LocalFilePreSynchronizer(final FileAlterationListener fileListener, final Collection<? extends FileEntity> mappedFiles) {
        this.fileListener = fileListener;
        this.mappedFiles = mappedFiles;
    }

    @Override
    public void run(final Collection<File> currentLocalFiles)  {
        final Map<Path, FileEntity> deletedLocalFiles = uploadChanged(currentLocalFiles, toMap(mappedFiles));
        deleteFromServer(deletedLocalFiles);
    }

    private void deleteFromServer(final Map<Path, FileEntity> dbEntryMap) {
        for (Path deletedLocalFile : dbEntryMap.keySet()) {
            fileListener.onFileDelete(deletedLocalFile.toFile());
        }
    }

    private Map<Path, FileEntity> toMap(final Collection<? extends FileEntity> list) {
        final Map<Path, FileEntity> map = new HashMap<>();
        for (FileEntity element : list) {
            map.put(Paths.get(element.getLocalPath()), element);
        }
        return map;
    }

    private Map<Path, FileEntity> uploadChanged(final Collection<File> currentLocalFiles, final Map<Path, FileEntity> mappedLocalFiles) {
        for (File localFile : currentLocalFiles) {
            if (localFile.isDirectory()) {
                uploadChanged(FileUtils.listFiles(localFile, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE), mappedLocalFiles);
            } else {
                final Path localFilePath = localFile.toPath();

                if (isMapped(localFilePath, mappedLocalFiles)) {
                    final FileEntity dbEntry = mappedLocalFiles.get(localFilePath);
                    uploadChanged(localFile, dbEntry);
                    mappedLocalFiles.remove(localFilePath);
                } else {
                    LOG.debug("New local file found: {}", localFilePath);
                    fileListener.onFileCreate(localFile);
                }
            }
        }
        return mappedLocalFiles;
    }

    private boolean isMapped(final Path localFilePath, final Map<Path, FileEntity> mappedLocalFiles) {
        return mappedLocalFiles.containsKey(localFilePath);
    }

    private void uploadChanged(final File localFile, final FileEntity dbEntry) {
        final Path localFilePath = localFile.toPath();
        if(isFileNewer(localFile, dbEntry.lastModified())) {
            LOG.debug("New local file has been updated before start: {}", localFilePath);
            fileListener.onFileChange(localFile);
        }
        else{
            LOG.debug("Untouched local file: {}", localFilePath);
        }
    }
}
