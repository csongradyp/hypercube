package com.noe.hypercube.observer.local;

import com.noe.hypercube.Action;
import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.domain.AccountBox;
import com.noe.hypercube.persistence.domain.IEntity;
import com.noe.hypercube.persistence.domain.UploadEntity;
import com.noe.hypercube.synchronization.SynchronizationException;
import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalFileListener implements FileAlterationListener {

    private static final Logger LOG = LoggerFactory.getLogger(LocalFileListener.class);
    private static final String TEMP_FILE_EXTENSION = "hyperTmp";

    private final Path targetDir;
    private final Collection<AccountBox> accountBoxes;
    private final IPersistenceController persistenceController;

    public LocalFileListener(final Path targetDir, final Collection<AccountBox> accountBoxes, final IPersistenceController persistenceController) {
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
        LOG.info("Directory creation detected: {}", directoryPath);
    }

    @Override
    public void onDirectoryChange(final File directory) {
        Path directoryPath = directory.toPath();
        LOG.info("Directory content change detected: {}", directoryPath);
    }

    @Override
    public void onDirectoryDelete(final File directory) {
        delete(directory);
    }

    @Override
    public void onFileCreate(final File file) {
        if(!isTemp(file)) {
            Path filePath = file.toPath();
            LOG.info("File creation detected: {}", filePath);
            upload(file);
        }
    }

    @Override
    public void onFileChange(final File file) {
        if(!isTemp(file)) {
            Path filePath = file.toPath();
            LOG.info("File update detected: {}", filePath);
            update(file);
        }
    }

    @Override
    public void onFileDelete(final File file) {
        if(!isTemp(file)) {
            Path filePath = file.toPath();
            LOG.info("File delete detected: {}", filePath);
            delete(file);
        }
    }

    private boolean isTemp(File file) {
        return FilenameUtils.getExtension(file.getName()).equals(TEMP_FILE_EXTENSION);
    }

    @Override
    public void onStop(final FileAlterationObserver observer) {
        for (AccountBox accountBox : accountBoxes) {
            accountBox.getDownloader().stop();
        }
    }

    private void delete(final File file) {
        forAllAccounts(file, (accountBox, remotePath) -> {
            final UploadEntity removedLocalContent = new UploadEntity(file, remotePath, Action.REMOVED);
            accountBox.getUploader().delete(removedLocalContent);
        });
    }

    private void update(final File file) {
        forAllAccounts(file, (accountBox, remotePath) -> {
            final UploadEntity changedLocalContent = new UploadEntity(file, remotePath, Action.CHANGED);
            accountBox.getUploader().uploadUpdated(changedLocalContent);
        });
    }

    private void upload(final File file) {
        forAllAccounts(file, (accountBox, remotePath) -> {
            final UploadEntity addedLocalContent = new UploadEntity(file, remotePath, getOrigin(file));
            accountBox.getUploader().uploadNew(addedLocalContent);
        });
    }

    private void forAllAccounts(final File file, final AccountActionCallback accountAction) {
        for (final AccountBox<?, ?, ?, ?> accountBox : accountBoxes) {
            try {
                List<Path> remotes = accountBox.getMapper().getRemotes(file);
                if (remotes != null) {
                    for (final Path mappedRemoteFolder : remotes) {
                        accountAction.call(accountBox, mappedRemoteFolder);
                    }
                }
            } catch (SynchronizationException e) {
                LOG.error(e.getMessage());
            }
        }
    }

    public String getOrigin(final File file) {
        final Set<Class<IEntity>> entityTypes = persistenceController.getEntitiesMapping(file.toPath().toString());
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
            return String.format("(%d)", System.currentTimeMillis());
        }
        LOG.debug("{} origin is Local", file);
        return "local";
    }

    public Path getTargetDir() {
        return targetDir;
    }
}
