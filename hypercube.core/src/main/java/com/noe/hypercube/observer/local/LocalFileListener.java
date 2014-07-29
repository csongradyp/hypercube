package com.noe.hypercube.observer.local;

import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.domain.AccountBox;
import com.noe.hypercube.domain.IEntity;
import com.noe.hypercube.domain.UploadEntity;
import com.noe.hypercube.synchronization.Action;
import com.noe.hypercube.synchronization.SynchronizationException;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class LocalFileListener implements FileAlterationListener {

    private static final Logger LOG = LoggerFactory.getLogger(LocalFileListener.class);

    private final Path targetDir;
    private final Collection<AccountBox> accountBoxes;
    private final IPersistenceController persistenceController;

    public LocalFileListener(Path targetDir, final Collection<AccountBox> accountBoxes, final IPersistenceController persistenceController) {
        this.targetDir = targetDir;
        this.accountBoxes = accountBoxes;
        this.persistenceController = persistenceController;
    }

    @Override
    public void onStart(final FileAlterationObserver observer) {

    }

    @Override
    public void onDirectoryCreate(final File directory) {
        Path directoryPath = directory.toPath();
        LOG.debug("Directory creation detected: {}", directoryPath);
    }

    @Override
    public void onDirectoryChange(final File directory) {
        Path directoryPath = directory.toPath();
        LOG.debug("Directory content change detected: {}", directoryPath);
    }

    @Override
    public void onDirectoryDelete(final File directory) {
        delete(directory);
    }

    @Override
    public void onFileCreate(final File file) {
        Path filePath = file.toPath();
        LOG.debug("File creation detected: {}", filePath);
        upload(file);
    }

    @Override
    public void onFileChange(final File file) {
        Path filePath = file.toPath();
        LOG.debug("File update detected: {}", filePath);
        update(file);
    }

    @Override
    public void onFileDelete(final File file) {
        Path filePath = file.toPath();
        LOG.debug("File delete detected: {}", filePath);
        delete(file);
    }

    @Override
    public void onStop(final FileAlterationObserver observer) {
        for (AccountBox accountBox : accountBoxes) {
            accountBox.stopUploader();
        }
    }

    private void delete(final File file) {
        for (AccountBox accountBox : accountBoxes) {
            try {
                List<Path> remotes = accountBox.getMapper().getRemotes(file);
                for (Path remote : remotes) {
                    final UploadEntity removedLocalContent = new UploadEntity(file, remote, Action.REMOVED);
                    accountBox.getUploader().delete(removedLocalContent);
                }
            } catch (SynchronizationException e) {
                LOG.error(e.getMessage());
            }
        }
    }

    private void update(final File file) {
        for (AccountBox accountBox : accountBoxes) {
            try {
                List<Path> remotes = accountBox.getMapper().getRemotes(file);
                for (Path remote : remotes) {
                    final UploadEntity changedLocalContent = new UploadEntity(file, remote, Action.CHANGED);
                    accountBox.getUploader().uploadUpdated(changedLocalContent);
                }
            } catch (SynchronizationException e) {
                LOG.error(e.getMessage());
            }
        }
    }

    private void upload(final File file) {
        for (AccountBox accountBox : accountBoxes) {
            try {
                List<Path> remotes = accountBox.getMapper().getRemotes(file);
                for (Path remote : remotes) {
                    final UploadEntity addedLocalContent = new UploadEntity(file, remote, getOrigin(file));
                    accountBox.getUploader().uploadNew(addedLocalContent);
                }
            } catch (SynchronizationException e) {
                LOG.error(e.getMessage());
            }
        }
    }

    public String getOrigin(final File file) {
        final Set<Class<IEntity>> entityTypes = persistenceController.getEntitiesMapping(file.toPath().toString().toString());
        final int foundOrigins = entityTypes.size();
        if (foundOrigins > 0) {
            if (foundOrigins == 1) {
                for (AccountBox accountBox : accountBoxes) {
                    if (accountBox.getClient().getEntityType().isAssignableFrom(entityTypes.iterator().next())) {
                        return accountBox.getClient().getAccountName();
                    }
                }

            }
            LOG.debug("{} origin is ambiguous", file);
            return "(" + System.currentTimeMillis() + ")";
        }
        LOG.debug("{} origin is Local", file);
        return "local";
    }

    public Path getTargetDir() {
        return targetDir;
    }
}
