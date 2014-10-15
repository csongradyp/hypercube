package com.noe.hypercube.synchronization.presynchronization.util;

import com.noe.hypercube.controller.IAccountController;
import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.domain.AccountBox;
import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.domain.UploadEntity;
import com.noe.hypercube.synchronization.Action;
import com.noe.hypercube.synchronization.SynchronizationException;
import com.noe.hypercube.synchronization.conflict.FileConflictNamingUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.noe.hypercube.synchronization.conflict.FileConflictNamingUtil.resolveFileName;

public class PreSynchronizationSubmitManager {

    private static final Logger LOG = LoggerFactory.getLogger(PreSynchronizationSubmitManager.class);

    @Inject
    private IPersistenceController persistenceController;
    @Inject
    private IAccountController accountController;

    public PreSynchronizationSubmitManager() {
    }

    public List<FileEntity> getAsResolved(final Collection<FileEntity> updateds) {
        List<FileEntity> resolvedRemoteFiles = new ArrayList<FileEntity>(updateds.size());
        for (FileEntity updated : updateds) {
            final FileEntity duplicated = updated.duplicate();
            resolveFileName(duplicated);
            resolvedRemoteFiles.add(duplicated);
        }
        return resolvedRemoteFiles;
    }


    public void deleteLocalFile(final File mappedLocalFile) {
        try {
            FileUtils.forceDelete(mappedLocalFile);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void updateFor(final File mappedLocalFile, final Collection<FileEntity> fileEntities, final Collection<AccountBox> accountBoxes) {
        for (FileEntity fileEntity : fileEntities) {
            updateFor(mappedLocalFile, fileEntity, accountBoxes);
        }
    }

    public void updateFor(final File mappedLocalFile, final FileEntity fileEntity, final Collection<AccountBox> accountBoxes) {
        try {
            final UploadEntity uploadEntity = new UploadEntity(mappedLocalFile, Paths.get(fileEntity.getRemotePath()), Action.CHANGED);
            for (AccountBox accountBox : accountBoxes) {
                accountBox.getUploader().uploadUpdated(uploadEntity);
            }
        } catch (SynchronizationException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void uploadAsNew(final File mappedLocalFile, final Collection<FileEntity> remoteFiles) {
        for (FileEntity remoteFile : remoteFiles) {
            uploadAsNew(mappedLocalFile, remoteFile);
        }
    }

    public void uploadAsNew(final Collection<FileEntity> remoteFiles) {
        for (FileEntity remoteFile : remoteFiles) {
            uploadAsNew(Paths.get(remoteFile.getLocalPath()).toFile(), remoteFile);
        }
    }

    public void uploadAsNew(final File mappedLocalFile, final FileEntity remoteFile) {
        final AccountBox accountBox = accountController.getAccountBox(remoteFile.getAccountName());
        try {
            final UploadEntity uploadEntity = new UploadEntity(mappedLocalFile, Paths.get(remoteFile.getRemotePath()), Action.ADDED);
            uploadEntity.setDependent(true);
            accountBox.getUploader().uploadUpdated(uploadEntity);
        } catch (SynchronizationException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void uploadAllAccountsAsNew(final File mappedLocalFile, final Collection<FileEntity> remoteFiles, final Collection<AccountBox> accountBoxes) {
        for (FileEntity remoteFile : remoteFiles) {
            uploadAllAccountsAsNew(mappedLocalFile, remoteFile, accountBoxes);
        }
    }

    public void uploadAllAccountsAsNew(final File mappedLocalFile, final FileEntity remoteFile, final Collection<AccountBox> accountBoxes) {
        try {
            final UploadEntity uploadEntity = new UploadEntity(mappedLocalFile, Paths.get(remoteFile.getRemotePath()), Action.ADDED);
            for (AccountBox accountBox : accountBoxes) {
                accountBox.getUploader().uploadNew(uploadEntity);
            }
        } catch (SynchronizationException e) {
            LOG.error(e.getMessage(), e);
        }
    }

//    public void moveDown(final Collection<FileEntity> remoteFiles) {
//        for (FileEntity remoteFile : remoteFiles) {
//            moveDown(remoteFile);
//        }
//    }
//
//    private void moveDown(final FileEntity remoteFile) {
//        final AccountBox accountBox = accountController.getAccountBox(remoteFile.getAccountName());
//        try {
//            final Path remoteFilePath = Paths.get(remoteFile.getRemotePath());
//            final Path localFilePath = Paths.get(remoteFile.getLocalPath());
//            accountBox.getDownloader().move(remoteFilePath, localFilePath);
//        } catch (SynchronizationException e) {
//            //            LOG.error(e.getMessage(), e);
//        }
//    }

    public void download(final Collection<FileEntity> remoteFiles) {
        for (FileEntity remoteFile : remoteFiles) {
            download(remoteFile);
        }
    }


    public void download(final FileEntity remoteFile) {
        final AccountBox accountBox = accountController.getAccountBox(remoteFile.getAccountName());
        try {
            final Path remotePath = Paths.get(remoteFile.getRemotePath());
            final Path localFolder = Paths.get(remoteFile.getLocalPath()).getParent();
            accountBox.getDownloader().download(remotePath, localFolder);
        } catch (SynchronizationException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void uploadUpdated(final File mappedLocalFile, final Collection<FileEntity> remoteFiles) {
        for (FileEntity remoteFile : remoteFiles) {
            uploadUpdated(mappedLocalFile, remoteFile);
        }
    }

    public void uploadUpdated(final File mappedLocalFile, final FileEntity remoteFile) {
        final AccountBox accountBox = accountController.getAccountBox(remoteFile.getAccountName());
        try {
            final UploadEntity uploadEntity = new UploadEntity(mappedLocalFile, Paths.get(remoteFile.getRemotePath()), Action.CHANGED);
            accountBox.getUploader().uploadUpdated(uploadEntity);
        } catch (SynchronizationException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void delete(final Collection<FileEntity> remoteFiles) {
        for (FileEntity fileEntity : remoteFiles) {
            delete(fileEntity);
        }
    }

    public void delete(final FileEntity remoteFile) {
        final AccountBox accountBox = accountController.getAccountBox(remoteFile.getAccountName());
        try {
            final UploadEntity uploadEntity = new UploadEntity(new File(remoteFile.getLocalPath()), Paths.get(remoteFile.getRemotePath()), Action.REMOVED);
            accountBox.getUploader().delete(uploadEntity);
        } catch (SynchronizationException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public Collection<FileEntity> renameRemoteWithResolvedName(final Collection<FileEntity> remoteFiles) throws SynchronizationException {
        Collection<FileEntity> renamedRemotes = new ArrayList<>(remoteFiles.size());
        for (FileEntity remoteFile : remoteFiles) {
            final String resolvedRemoteFileName = FileConflictNamingUtil.getResolveFileName(remoteFile);
            final FileEntity renamedRemoteWithResolvedName = renameRemoteWithResolvedName(remoteFile, resolvedRemoteFileName);
            renamedRemotes.add(renamedRemoteWithResolvedName);
        }
        return renamedRemotes;
    }

    public FileEntity renameRemoteWithResolvedName(final FileEntity remoteFile, final String newName) throws SynchronizationException {
        final AccountBox accountBox = accountController.getAccountBox(remoteFile.getAccountName());
        return accountBox.getClient().rename(remoteFile, newName);
    }

    public void uploadAsNewTo(final Collection<FileEntity> remoteFiles, final FileEntity destination) {
        for (FileEntity remoteFile : remoteFiles) {
            uploadAsNewTo(remoteFile, destination);
        }
    }

    public void uploadAsNewTo(final FileEntity remoteFile, final FileEntity destination) {
        final AccountBox accountBox = accountController.getAccountBox(destination.getAccountName());
        final File localFile = new File(remoteFile.getLocalPath());
        final Path remoteFolder = Paths.get(destination.getRemotePath()).getParent();
        try {
            final UploadEntity uploadEntity = new UploadEntity(localFile, remoteFolder, Action.ADDED);
            uploadEntity.setDependent(true);
            accountBox.getUploader().uploadNew(uploadEntity);
        } catch (SynchronizationException e) {
            LOG.error(e.getMessage(), e);
        }

    }

    public void uploadAsNewResolvedTo(final Collection<FileEntity> remoteFiles, final FileEntity destination) {
        for (FileEntity remoteFile : remoteFiles) {
            final Path localFolder = Paths.get(remoteFile.getLocalPath()).getParent();
            remoteFile.setLocalPath(localFolder + FilenameUtils.getName(remoteFile.getRemotePath()));
            uploadAsNewTo(remoteFile, destination);
        }
    }
}
