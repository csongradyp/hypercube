package com.noe.hypercube.observer;

import com.noe.hypercube.synchronization.SynchronizationException;
import com.noe.hypercube.synchronization.upstream.IUploader;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class LocalFileListener implements FileAlterationListener {

    private static final Logger LOG = LoggerFactory.getLogger(LocalFileListener.class);

    private List<IUploader> uploaders;

    public LocalFileListener(List<IUploader> uploaders) {
        this.uploaders = uploaders;
    }

    @Override
    public void onStart(FileAlterationObserver observer) {}

    @Override
    public void onDirectoryCreate(File directory) {
        Path directoryPath = Paths.get(directory.toURI());
        LOG.debug("Directory creation detected: {}", directoryPath);
    }

    @Override
    public void onDirectoryChange(File directory) {
        Path directoryPath = Paths.get(directory.toURI());
        LOG.debug("Directory content change detected: {}", directoryPath);
    }

    @Override
    public void onDirectoryDelete(File directory) {
        Path directoryPath = Paths.get(directory.toURI());
        try {
            for (IUploader uploader : uploaders) {
                uploader.delete(directoryPath);
            }
            LOG.debug("Directory deleted: " +  directory.getCanonicalPath());
        } catch (IOException | SynchronizationException e) {
            LOG.error("Failed to delete Directory: " + directory.getPath(), e);
        }
    }

    @Override
    public void onFileCreate(File file) {
        Path filePath = Paths.get(file.toURI());
        LOG.debug("File creation detected: " + filePath);
        try {
            for (IUploader upstreamSynchronizer : uploaders) {
                upstreamSynchronizer.uploadNew(filePath);
            }
        } catch (SynchronizationException e) {
            LOG.error("Error at File creation detection", e);
        }
    }

    @Override
    public void onFileChange(File file) {
        Path filePath = Paths.get(file.toURI());
        LOG.debug("File update detected: " + filePath);
        try {
            for (IUploader upstreamSynchronizer : uploaders) {
                upstreamSynchronizer.uploadUpdated(filePath);
            }
        } catch (SynchronizationException e) {
            LOG.error("Error during update file: " + file.getName(), e);
        }
    }

    @Override
    public void onFileDelete(File file) {
        Path filePath = Paths.get(file.toURI());
        LOG.debug("File delete detected: " + filePath);
        try {
            for (IUploader upstreamSynchronizer : uploaders) {
                upstreamSynchronizer.delete(file);
            }
        } catch (SynchronizationException e) {
            LOG.error("Failed to delete File: " + file.getPath(), e);
        }
    }

    @Override
    public void onStop(FileAlterationObserver observer) {}
}
