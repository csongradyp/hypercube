package com.noe.hypercube.synchronization.presynchronization;

import com.noe.hypercube.controller.IAccountController;
import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.domain.*;
import com.noe.hypercube.synchronization.Action;
import com.noe.hypercube.synchronization.SynchronizationException;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.noe.hypercube.synchronization.conflict.FileConflictNamingUtil.resolveFileName;

public class FolderPreSynchronizer implements IPreSynchronizer {

    private static final Logger LOG = LoggerFactory.getLogger(FolderPreSynchronizer.class);
    @Inject
    private IPersistenceController persistenceController;
    @Inject
    private IAccountController accountController;

    private final List<AccountBox> accountBoxes;
    private final Path targetFolder;

    public FolderPreSynchronizer(final Path targetFolder) {
        this.targetFolder = targetFolder;
        accountBoxes = new ArrayList<>();
        collectAccountsMapping(targetFolder);
    }

    private void collectAccountsMapping(final Path targetFolder) {
        final List<MappingEntity> mappings = persistenceController.getMappings(targetFolder.toString());
        for (MappingEntity mapping : mappings) {
            accountBoxes.add(accountController.getAccountBox(mapping.getAccountType()));
        }
    }

    @Override
    public void run() {
        final Map<String, Collection<ServerEntry>> remoteFileLists = createRemoteFileList();
        final Collection<File> localFiles = FileUtils.listFiles(targetFolder.toFile(), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        final Map<String, List<FileEntity>> files = persistenceController.getMappedEntities(targetFolder.toString());

        for (String localFilePath : files.keySet()) {
            final File mappedLocalFile = Paths.get(localFilePath).toFile();
            final List<FileEntity> mappings = files.get(localFilePath);
            final Map<Action, Collection<FileEntity>> sort = sort(mappings, remoteFileLists);
            final Collection<FileEntity> updateds = sort.get(Action.CHANGED);
            // local file exist scenarios
            if (localFiles.contains(mappedLocalFile)) {
                final Collection<FileEntity> identicals = sort.get(Action.IDENTICAL);
                if (isLocalFileChanged(mappedLocalFile)) {
                    // local file updated scenarios
                    localFileUpdatedScenarios(mappedLocalFile, sort);
                }
                else {
                    // local file is identical scenarios
                    localFileIdenticalScenarios(mappedLocalFile, sort);
                }
            } else {
                // local file deleted scenario
                localFileDeletedScenario(mappedLocalFile, sort);
            }
        }
    }

    private void localFileIdenticalScenarios(File mappedLocalFile, Map<Action, Collection<FileEntity>> sort) {
        final Collection<FileEntity> identicals = sort.get(Action.IDENTICAL);
        final Collection<FileEntity> updateds = sort.get(Action.CHANGED);
        final Collection<FileEntity> deleteds = sort.get(Action.REMOVED);
        if (updateds.isEmpty()) {
            // no updates
            if (deleteds.isEmpty()) {
                // all identical - nothing to do
                LOG.info("All remote spaces are identical for {}", mappedLocalFile.getName());
            } else {
                // delete identical files - all remaining -> there are no updated
                deleteLocalFile(mappedLocalFile);
                delete(identicals);
            }
        } else if (updateds.size() == 1) {
            // single update found
            final FileEntity updatedRemoteFile = updateds.iterator().next();
            download(updatedRemoteFile);
            if (deleteds.isEmpty()) {
                // all files are identical except the updated one -> update all
                // FIXME - local file must exist at this time - must wait the download
                uploadAsNew(mappedLocalFile, updatedRemoteFile);
            } else {
                // upload updated file to the deleted ones remote space -> add + update all
                uploadAsNew(mappedLocalFile, deleteds);
            }
        } else {
            // conflict - which update to choose?
            // download all updated files  with resolved names
            for (FileEntity updatedRemoteFile : updateds) {
                resolveFileName(updatedRemoteFile);
                download(updatedRemoteFile);
            }
        }
    }

    private void localFileUpdatedScenarios(File mappedLocalFile, Map<Action, Collection<FileEntity>> sort) {
        final Collection<FileEntity> deleted = sort.get(Action.REMOVED);
        final Collection<FileEntity> identicals = sort.get(Action.IDENTICAL);
        final Collection<FileEntity> updateds = sort.get(Action.CHANGED);
        if (updateds.isEmpty()) {
            // no remote file has been updated
            if (deleted.isEmpty()) {
                // no remote changes - update updated local file for all accounts
                uploadUpdated(mappedLocalFile, identicals);
            } else {
                // all remotes deleted - upload updated local file as new
                uploadAllAccountsAsNew(deleted);
            }
        } else {
            // there are updated files
            if (deleted.isEmpty()) {
                // there are just updated remote files
            } else {

            }
        }
    }

    private void localFileDeletedScenario(File mappedLocalFile, Map<Action, Collection<FileEntity>> sort) {
        final Collection<FileEntity> identicals = sort.get(Action.IDENTICAL);
        final Collection<FileEntity> updateds = sort.get(Action.CHANGED);
        final Collection<FileEntity> deleteds = sort.get(Action.REMOVED);
        if (updateds.isEmpty()) {
            // no updates
            if (deleteds.isEmpty()) {
                // delete identical remote files
                delete(identicals);
            } else {
                // all files deleted - remote and local
            }
            // single update found
        } else if (updateds.size() == 1) {
            final FileEntity updatedRemoteFile = updateds.iterator().next();
            // download updated file -> restore deleted local file
            download(updatedRemoteFile);
            // all files are identical except the updated one -> update all
            if (deleteds.isEmpty()) {
                // removed local file has been restored with the updated one - update identical remotes with it
                updateAllAccounts(mappedLocalFile, identicals);
                // there are deleted remote files except the updated one
            } else {
                // upload updated file as a new file to the deleted remote spaces
                uploadAsNew(mappedLocalFile, deleteds);
                // upload updated file to the identical remote spaces
                uploadUpdated(mappedLocalFile, identicals);
            }
            // conflict - resolve all names with account
        } else {
            // TODO original updated files should be removed too - resolveFileName should create new Entity ????
            List<FileEntity> resolvedRemoteFiles  = GetAsResolved(updateds);
            // download all updated files  with resolved names
//            downloadWithResolvedName(updateds);
            download(resolvedRemoteFiles);
            // delete identical remote files
            delete(identicals);
            // delete old updated remote files
            delete(updateds);
            // upload resolved for all remote drives
            // TODO
            uploadAsNew(mappedLocalFile, identicals);
        }
    }

    private List<FileEntity> GetAsResolved(Collection<FileEntity> updateds) {
        List<FileEntity> resolvedRemoteFiles = new ArrayList<>(updateds.size());
        for (FileEntity updated : updateds) {
            final FileEntity duplicated = updated.duplicate();
            resolveFileName(duplicated);
            resolvedRemoteFiles.add(duplicated);
        }
        return resolvedRemoteFiles;
    }

    private Map<String, Collection<ServerEntry>> createRemoteFileList() {
        final Map<String, Collection<ServerEntry>> remoteFileLists = new HashMap<>();
        final Collection<AccountBox> accountBoxes = accountController.getAll();
        for (AccountBox accountBox : accountBoxes) {
            try {
                final Path remoteFolder = persistenceController.getRemoteFolder(accountBox.getClient().getMappingType(), targetFolder);
                if (remoteFolder != null) {
                    // local folder is mapped for account
                    final List<ServerEntry> fileList = accountBox.getClient().getFileList(remoteFolder);
                    remoteFileLists.put(accountBox.getAccountType().getName(), fileList);
                }
            } catch (SynchronizationException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return remoteFileLists;
    }

    private boolean isLocalFileChanged(final File mappedLocalFile) {
        try {
            final Long currentLocalFileCrc = FileUtils.checksumCRC32(mappedLocalFile);
            final LocalFileEntity storedLocalFileEntity = persistenceController.getLocalFileEntity(mappedLocalFile.toPath());
            final Long lastSyncedLocalFileCbc = storedLocalFileEntity.getCrc();
            if (!lastSyncedLocalFileCbc.equals(currentLocalFileCrc)) {
                return true;
            }
        } catch (IOException e) {
            return true;
        }
        return false;
    }

    private void updateAllAccounts(final File mappedLocalFile, final Collection<FileEntity> fileEntities) {
        for (FileEntity fileEntity : fileEntities) {
            updateAllAccounts(mappedLocalFile, fileEntity);
        }
    }

    private void updateAllAccounts(final File mappedLocalFile, final FileEntity fileEntity) {
        try {
            final UploadEntity uploadEntity = new UploadEntity(mappedLocalFile, Paths.get(fileEntity.getRemotePath()), Action.CHANGED);
            for (AccountBox accountBox : accountBoxes) {
                accountBox.getUploader().uploadUpdated(uploadEntity);
            }
        } catch (SynchronizationException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void uploadAsNew(final File mappedLocalFile, final Collection<FileEntity> fileEntities) {
        for (FileEntity fileEntity : fileEntities) {
            uploadAsNew(mappedLocalFile, fileEntity);
        }
    }

    private void uploadAsNew(final File mappedLocalFile, final FileEntity deletedRemoteFile) {
        try {
            final UploadEntity uploadEntity = new UploadEntity(mappedLocalFile, Paths.get(deletedRemoteFile.getRemotePath()), Action.ADDED);
            uploadEntity.setDependent(new LocalFileEntity(mappedLocalFile));
            accountController.getAccountBox(deletedRemoteFile.getAccountName()).getUploader().uploadUpdated(uploadEntity);
        } catch (SynchronizationException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void uploadAllAccountsAsNew(final Collection<FileEntity> remoteFiles) {
        for (FileEntity remoteFile : remoteFiles) {
            uploadAllAccountsAsNew(remoteFile);
        }
    }

    private void uploadAllAccountsAsNew(final FileEntity remoteFile) {
        try {
            final UploadEntity uploadEntity = new UploadEntity(new File(remoteFile.getLocalPath()), Paths.get(remoteFile.getRemotePath()), Action.ADDED);
            for (AccountBox accountBox : accountBoxes) {
                accountBox.getUploader().uploadNew(uploadEntity);
            }
        } catch (SynchronizationException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void download(final Collection<FileEntity> remoteFiles) {
        for (FileEntity remoteFile : remoteFiles) {
            download(remoteFile);
        }
    }

    private void downloadWithResolvedName(final Collection<FileEntity> remoteFiles) {
        for (FileEntity remoteFile : remoteFiles) {
            final FileEntity duplicated = remoteFile.duplicate();
            resolveFileName(duplicated);
            download(duplicated);
        }
    }

    private void download(final FileEntity remoteFile) {
        final AccountBox accountBox = accountController.getAccountBox(remoteFile.getAccountName());
        try {
            final Path remotePath = Paths.get(remoteFile.getRemotePath());
            final Path localFolder = Paths.get(remoteFile.getLocalPath()).getParent();
            accountBox.getDownloader().download(remotePath, localFolder);
        } catch (SynchronizationException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void uploadUpdated(File mappedLocalFile, Collection<FileEntity> remoteFiles) {
        for (FileEntity remoteFile : remoteFiles) {
            uploadUpdated(mappedLocalFile, remoteFile);
        }
    }

    private void uploadUpdated(File mappedLocalFile, FileEntity remoteFile) {
        try {
            final UploadEntity uploadEntity = new UploadEntity(mappedLocalFile, Paths.get(remoteFile.getRemotePath()), Action.CHANGED);
            accountController.getAccountBox(remoteFile.getAccountName()).getUploader().uploadUpdated(uploadEntity);
        } catch (SynchronizationException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void delete(final Collection<FileEntity> fileEntities) {
        for (FileEntity fileEntity : fileEntities) {
            delete(fileEntity);
        }
    }

    private void delete(final FileEntity fileEntity) {
        try {
            final UploadEntity uploadEntity = new UploadEntity(new File(fileEntity.getLocalPath()), Paths.get(fileEntity.getRemotePath()), Action.REMOVED);
            accountController.getAccountBox(fileEntity.getAccountName()).getUploader().delete(uploadEntity);
        } catch (SynchronizationException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private Map<Action, Collection<FileEntity>> sort(final List<FileEntity> mappings, final Map<String, Collection<ServerEntry>> remoteFileLists) {
        final Map<Action, Collection<FileEntity>> actionMap = new EnumMap<>(Action.class);
        final Collection<FileEntity> updated = new ArrayList<>();
        final Collection<FileEntity> deleted = new ArrayList<>();
        final Collection<FileEntity> identical = new ArrayList<>();
        // sort local file mappings by action
        for (FileEntity mapping : mappings) {
            final Collection<ServerEntry> accountFileList = remoteFileLists.get(mapping.getAccountName());
            for (ServerEntry serverEntry : accountFileList) {
                if (serverEntry.getPath().equals(Paths.get(mapping.getRemotePath()))) {
                    if (serverEntry.getRevision().equals(mapping.getRevision())) {
                        identical.add(mapping);
                    } else {
                        updated.add(mapping);
                    }
                } else {
                    deleted.add(mapping);
                }
            }
        }
        actionMap.put(Action.REMOVED, deleted);
        actionMap.put(Action.CHANGED, updated);
        actionMap.put(Action.IDENTICAL, identical);
        return actionMap;
    }

    private Map<String, List<FileEntity>> toMap(Collection<FileEntity> mappedFiles) {
        final HashMap<String, List<FileEntity>> map = new HashMap<>();
        for (FileEntity mappedFile : mappedFiles) {
            if (map.containsKey(mappedFile.getLocalPath())) {
                map.get(mappedFile.getLocalPath()).add(mappedFile);
            }
        }
        return map;
    }

    private void deleteLocalFile(File mappedLocalFile) {
        try {
            FileUtils.forceDelete(mappedLocalFile);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private ServerEntry findRemote(Collection<ServerEntry> remoteFileList, Map.Entry<String, String> mapping) {
        for (ServerEntry serverEntry : remoteFileList) {
            if (serverEntry.getPath().toString().equals(mapping.getValue())) {
                return serverEntry;
            }
        }
        return null;
    }

    private File find(Collection<File> localFiles, Map.Entry<String, String> mapping) {
        for (File localFile : localFiles) {
            if (localFile.toPath().toString().equals(mapping.getKey())) {
                return localFile;
            }
        }
        return null;
    }

    private boolean isMapped(BidiMap<String, String> mappings, File localFile) {
        return mappings.get(localFile.toPath().toString()) != null;
    }

    private BidiMap<String, String> getMappedFiles(Collection<FileEntity> mappedFiles) {
        final BidiMap<String, String> mappings = new DualHashBidiMap<>();
        for (FileEntity mappedFile : mappedFiles) {
            mappings.put(mappedFile.getLocalPath(), mappedFile.getRemotePath());
        }
        return mappings;
    }

    private Collection<File> getDeletedLocalFiles(Collection<FileEntity> mappedFiles, Collection<File> LocalFileList) {
        final Collection<File> deletedFiles = new ArrayList<>();
        getMappedFiles(mappedFiles);

        return deletedFiles;
    }

    @Override
    public Path getTargetFolder() {
        return targetFolder;
    }
}
