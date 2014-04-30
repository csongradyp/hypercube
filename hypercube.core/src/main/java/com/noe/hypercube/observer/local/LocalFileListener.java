package com.noe.hypercube.observer.local;

import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.domain.MappingEntity;
import com.noe.hypercube.mapping.DirectoryMapper;
import com.noe.hypercube.service.Account;
import com.noe.hypercube.synchronization.SynchronizationException;
import com.noe.hypercube.synchronization.upstream.IUploader;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class LocalFileListener<ACCOUNT_TYPE extends Account> implements FileAlterationListener {

    private static final Logger LOG = LoggerFactory.getLogger(LocalFileListener.class);

    private DirectoryMapper<ACCOUNT_TYPE, ? extends MappingEntity> mapper;
    private IUploader<ACCOUNT_TYPE, ? extends FileEntity> uploader;

    public LocalFileListener(IUploader<ACCOUNT_TYPE, ? extends FileEntity> uploader, DirectoryMapper<ACCOUNT_TYPE, ? extends MappingEntity> mapper) {
        this.uploader = uploader;
        this.mapper = mapper;
    }

    @Override
    public void onStart(FileAlterationObserver observer) {}

    @Override
    public void onDirectoryCreate(File directory) {
        Path directoryPath = directory.toPath();
        LOG.debug("Directory creation detected: {}", directoryPath);
    }

    @Override
    public void onDirectoryChange(File directory) {
        Path directoryPath = directory.toPath();
        LOG.debug("Directory content change detected: {}", directoryPath);
    }

    @Override
    public void onDirectoryDelete(File directory) {
        delete(directory);
    }

    @Override
    public void onFileCreate(File file) {
        Path filePath = file.toPath();
        LOG.debug("File creation detected: " + filePath);
        upload(file);
    }

    @Override
    public void onFileChange(File file) {
        Path filePath = file.toPath();
        LOG.debug("File update detected: " + filePath);
        update(file);
    }

    @Override
    public void onFileDelete(File file) {
        Path filePath = file.toPath();
        LOG.debug("File delete detected: " + filePath);
        delete(file);
    }

    @Override
    public void onStop(FileAlterationObserver observer) {}

    private void delete(File file) {
        try {
            List<Path> remotes = mapper.getRemotes(file);
            for (Path remote : remotes) {
                uploader.delete(file, remote);
            }
        } catch (SynchronizationException e) {
            LOG.error(e.getMessage());
        }
    }

    private void update(File file) {
        try {
            List<Path> remotes = mapper.getRemotes(file);
            for (Path remote : remotes) {
                uploader.uploadUpdated(file, remote);
            }
        } catch (SynchronizationException e) {
            LOG.error(e.getMessage());
        }
    }

    private void upload(File file) {
        try {
            List<Path> remotes = mapper.getRemotes(file);
            for (Path remote : remotes) {
                uploader.uploadNew(file, remote);
            }
        } catch (SynchronizationException e) {
            LOG.error(e.getMessage());
        }
    }

    public IUploader<ACCOUNT_TYPE, ? extends FileEntity> getUploader() {
        return uploader;
    }
}