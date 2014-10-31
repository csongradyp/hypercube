package com.noe.hypercube.synchronization.presynchronization.util;

import com.noe.hypercube.Action;
import com.noe.hypercube.controller.IAccountController;
import com.noe.hypercube.domain.*;
import com.noe.hypercube.service.Account;
import com.noe.hypercube.synchronization.SynchronizationException;
import com.noe.hypercube.synchronization.conflict.FileConflictNamingUtil;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
public class PreSynchronizationSubmitManager {

    private static final Logger LOG = LoggerFactory.getLogger(PreSynchronizationSubmitManager.class);

    @Inject
    private IAccountController accountController;

    public void updateFor(final File mappedLocalFile, final Collection<FileEntity> fileEntities, final Collection<AccountBox> accountBoxes) {
        for (FileEntity fileEntity : fileEntities) {
            updateFor(mappedLocalFile, fileEntity, accountBoxes);
        }
    }

    public void updateFor(final File mappedLocalFile, final FileEntity remoteFile, final Collection<AccountBox> accountBoxes) {
        try {
            final UploadEntity uploadEntity = new UploadEntity(mappedLocalFile, Paths.get(remoteFile.getRemotePath()), Action.CHANGED);
            for (AccountBox accountBox : accountBoxes) {
                LOG.debug("Update {} file: {}", remoteFile.getLocalPath(), remoteFile.getRemotePath(), accountBox.getClient().getAccountName());
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

    public void uploadAllAccountsAsNew(final Collection<File> locals, final Map<Class<? extends Account>, Collection<Path>> accountRemoteFolders, final Collection<AccountBox> accountBoxes) {
        for (File localFile : locals) {
            uploadAllAccountsAsNew(localFile, accountRemoteFolders, accountBoxes);
        }
    }

    public void uploadAllAccountsAsNew(final File localFile, final Map<Class<? extends Account>, Collection<Path>> accountRemoteFolders, final Collection<AccountBox> accountBoxes) {
        for (AccountBox accountBox : accountBoxes) {
            final Collection<Path> remoteFolders = accountRemoteFolders.get(accountBox.getAccountType());
            for (Path remoteFolder : remoteFolders) {
                uploadAsNew(localFile, remoteFolder, accountBox);
            }
        }
    }

    private void uploadAsNew(final File localFile, final Path remoteFolder, final AccountBox accountBox) {
        LOG.debug("Upload file {} as new to {} to account {}", localFile.toPath(), remoteFolder, accountBox.getClient().getAccountName());
        final UploadEntity uploadEntity = new UploadEntity(localFile, remoteFolder, Action.ADDED);
        try {
            uploadEntity.setDependent(true);
            accountBox.getUploader().uploadNew(uploadEntity);
        } catch (SynchronizationException e) {
                    LOG.error(e.getMessage(), e);
        }
    }

    public void download(final Collection<FileEntity> remoteFiles) {
        for (FileEntity remoteFile : remoteFiles) {
            download(remoteFile);
        }
    }

    public void download(final FileEntity remoteFile) {
        final AccountBox accountBox = accountController.getAccountBox(remoteFile.getAccountName());
        final Path remotePath = Paths.get(remoteFile.getRemotePath());
        accountBox.getDownloader().download(new FileServerEntry(accountBox.getClient().getAccountName(), remotePath));
    }

    public void uploadUpdated(final File mappedLocalFile, final Collection<FileEntity> remoteFiles) {
        for (FileEntity remoteFile : remoteFiles) {
            uploadUpdated(mappedLocalFile, remoteFile);
        }
    }

    public void uploadUpdated(final File mappedLocalFile, final FileEntity remoteFile) {
        LOG.debug("Update file {} as {} to account {}", remoteFile.getLocalPath(), remoteFile.getRemotePath(), remoteFile.getAccountName());
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
        LOG.debug("Delete {} file {}", remoteFile.getAccountName(), remoteFile.getRemotePath());
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
            final String resolvedRemoteFileName = FileConflictNamingUtil.getResolvedFileName(remoteFile);
            final FileEntity renamedRemoteWithResolvedName = renameRemoteWithResolvedName(remoteFile, resolvedRemoteFileName);
            renamedRemotes.add(renamedRemoteWithResolvedName);
        }
        return renamedRemotes;
    }

    public FileEntity renameRemoteWithResolvedName(final FileEntity remoteFile, final String newName) throws SynchronizationException {
        final AccountBox accountBox = accountController.getAccountBox(remoteFile.getAccountName());
        return accountBox.getClient().rename(remoteFile, newName);
    }

    public Collection<FileEntity> renameRemotesWithResolvedName(final Collection<ServerEntry> remoteFiles) throws SynchronizationException {
        Collection<FileEntity> renamedRemotes = new ArrayList<>(remoteFiles.size());
        for (ServerEntry remoteFile : remoteFiles) {
            final String resolvedRemoteFileName = FileConflictNamingUtil.getResolvedFileName(remoteFile);
            final FileEntity renamedRemoteWithResolvedName = renameRemote(remoteFile, resolvedRemoteFileName);
            renamedRemotes.add(renamedRemoteWithResolvedName);
        }
        return renamedRemotes;
    }

    public FileEntity renameRemote(final ServerEntry remoteFile, final String newName) throws SynchronizationException {
        final AccountBox accountBox = accountController.getAccountBox(remoteFile.getAccount());
        return accountBox.getClient().rename(remoteFile, newName);
    }

    public void uploadAsNewResolvedTo(final Collection<FileEntity> remoteFiles, final Path remoteFolder, final AccountBox accountBox) {
        for (FileEntity remoteFile : remoteFiles) {
            final Path localFolder = Paths.get(remoteFile.getLocalPath()).getParent();
            remoteFile.setLocalPath(localFolder + FilenameUtils.getName(remoteFile.getRemotePath()));
            uploadAsNewTo(remoteFile, remoteFolder, accountBox);
        }
    }


    public void uploadAsNewTo(final FileEntity remoteFile, final Path remoteFolder, final AccountBox accountBox) {
        LOG.debug("Upload file {} as new as {} to account {}", remoteFile.getLocalPath(), remoteFile.getRemotePath(), remoteFile.getAccountName());
        final File localFile = new File(remoteFile.getLocalPath());
        try {
            final UploadEntity uploadEntity = new UploadEntity(localFile, remoteFolder, Action.ADDED);
            uploadEntity.setDependent(true);
            accountBox.getUploader().uploadNew(uploadEntity);
        } catch (SynchronizationException e) {
            e.printStackTrace();
        }
    }

    public void download(final Map<String, Collection<ServerEntry>> serverEntries, final Map<String, AccountBox> accountBoxes) {
        for (String account : serverEntries.keySet()) {
            final AccountBox accountBox = accountBoxes.get(account);
            final Collection<ServerEntry> accountEntries = serverEntries.get(account);
            download(accountEntries, accountBox);
        }
    }

    public void download(final Collection<ServerEntry> serverEntries, final AccountBox accountBox) {
        for (ServerEntry serverEntry : serverEntries) {
            accountBox.getDownloader().download(serverEntry);
        }
    }
}
