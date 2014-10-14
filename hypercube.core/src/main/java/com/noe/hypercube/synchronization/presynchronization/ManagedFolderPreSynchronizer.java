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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.noe.hypercube.synchronization.conflict.FileConflictNamingUtil.resolveFileName;

public class ManagedFolderPreSynchronizer implements IPreSynchronizer {

    private static final Logger LOG = LoggerFactory.getLogger(FolderPreSynchronizer.class);

    private final IPersistenceController persistenceController;
    private final IAccountController accountController;
    private final Set<AccountBox> accountBoxes;
    private final Path targetFolder;

    public ManagedFolderPreSynchronizer(final Path targetFolder, IPersistenceController persistenceController, IAccountController accountController) {
        this.targetFolder = targetFolder;
        this.persistenceController = persistenceController;
        this.accountController = accountController;
        accountBoxes = new HashSet<>();
    }

    @Override
    public void run() {
        collectAccountBoxes(targetFolder);
        final Map<String, Collection<ServerEntry>> remoteFileLists = createRemoteFileList();
        final Collection<File> localFiles = FileUtils.listFiles(targetFolder.toFile(), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        final Map<String, List<FileEntity>> mappedFilesByLocalPath = persistenceController.getMappedEntities(targetFolder.toString());

        for (Map.Entry<String, List<FileEntity>> mappedFilesEntity : mappedFilesByLocalPath.entrySet()) {
            final File mappedLocalFile = Paths.get(mappedFilesEntity.getKey()).toFile();
            final List<FileEntity> mappedRemoteFiles = mappedFilesEntity.getValue();
            final ManagedMappings sortedMappings = getMappingsByAction(mappedRemoteFiles, remoteFileLists);
            // local file exist scenarios
            if (localFiles.contains(mappedLocalFile)) {
                if (isLocalFileChanged(mappedLocalFile)) {
                    // local file updated scenarios
                    localFileUpdatedScenarios(mappedLocalFile, sortedMappings);
                } else {
                    // local file is identical scenarios
                    localFileIdenticalScenarios(mappedLocalFile, sortedMappings);
                }
            } else {
                // local file deleted scenario
                localFileDeletedScenario(mappedLocalFile, sortedMappings);
            }
            final AddedFiles addedFiles = getAddedFiles(remoteFileLists, localFiles, sortedMappings);
        }
    }

    private void collectAccountBoxes(final Path targetFolder) {
        final List<MappingEntity> mappings = persistenceController.getMappings(targetFolder.toString());
        for (MappingEntity mapping : mappings) {
            accountBoxes.add(accountController.getAccountBox(mapping.getAccountType()));
        }
    }

    private void localFileIdenticalScenarios(File mappedLocalFile, ManagedMappings sort) {
        final Collection<FileEntity> identicals = sort.getIdenticals();
        final Collection<FileEntity> updateds = sort.getUpdateds();
        final Collection<FileEntity> deleteds = sort.getDeleteds();
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

    private void localFileUpdatedScenarios(File mappedLocalFile, ManagedMappings sort) {
        final Collection<FileEntity> identicals = sort.getIdenticals();
        final Collection<FileEntity> updateds = sort.getUpdateds();
        final Collection<FileEntity> deleteds = sort.getDeleteds();
        if (updateds.isEmpty()) {
            // no remote file has been updated
            if (deleteds.isEmpty()) {
                // no remote changes - update updated local file for all accounts
                uploadUpdated(mappedLocalFile, identicals);
            } else {
                // all remotes deleted - upload updated local file as new
                uploadAllAccountsAsNew(deleteds);
            }
        } else {
            if (deleteds.isEmpty()) {
                // there are updated files
            } else {
                // there are just updated remote files

            }
        }
    }

    private void localFileDeletedScenario(File mappedLocalFile, ManagedMappings sort) {
        final Collection<FileEntity> identicals = sort.getIdenticals();
        final Collection<FileEntity> updateds = sort.getUpdateds();
        final Collection<FileEntity> deleteds = sort.getDeleteds();
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
            List<FileEntity> resolvedRemoteFiles = getAsResolved(updateds);
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

    private List<FileEntity> getAsResolved(Collection<FileEntity> updateds) {
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
                    remoteFileLists.put(accountBox.getClient().getAccountName(), fileList);
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

    private void uploadAsNew(final File mappedLocalFile, final FileEntity remoteFile) {
        try {
            final UploadEntity uploadEntity = new UploadEntity(mappedLocalFile, Paths.get(remoteFile.getRemotePath()), Action.ADDED);
            uploadEntity.setDependent(new LocalFileEntity(mappedLocalFile));
            accountController.getAccountBox(remoteFile.getAccountName()).getUploader().uploadUpdated(uploadEntity);
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

    private ManagedMappings getMappingsByAction(final List<FileEntity> mappedRemoteFiles, final Map<String, Collection<ServerEntry>> remoteFileLists) {
        final ManagedMappings managedMappings = new ManagedMappings(mappedRemoteFiles);
        // sort local file mappings by action
        for (FileEntity mappedRemoteFile : mappedRemoteFiles) {
            final String accountName = mappedRemoteFile.getAccountName();
            final Collection<ServerEntry> accountFileList = remoteFileLists.get(accountName);
            for (ServerEntry serverEntry : accountFileList) {
                if (serverEntry.getPath().equals(Paths.get(mappedRemoteFile.getRemotePath()))) {
                    if (isSameRevision(mappedRemoteFile, serverEntry)) {
//                    if (revision.equals(revision1)) {
                        managedMappings.addIdentical(mappedRemoteFile);
                    } else {
                        managedMappings.addUpdated(mappedRemoteFile);
                    }
                }
            }
        }

        return managedMappings;
    }

    private AddedFiles getAddedFiles(Map<String, Collection<ServerEntry>> remoteFileLists, Collection<File> localFiles, ManagedMappings sortedMappings) {
        final AddedFiles addedFiles = new AddedFiles();
        getRemoteAddeds(addedFiles, remoteFileLists, sortedMappings);
        final Collection<File> localAddeds = getLocalAddeds(localFiles);
        addedFiles.addLocals(localAddeds);
        return addedFiles;
    }

    private Collection<File> getLocalAddeds(final Collection<File> localFiles) {
        final Collection<File> addedLocalFiles = new ArrayList<>(localFiles);
        final Iterator<File> iterator = addedLocalFiles.iterator();
        while (iterator.hasNext()) {
            final File localFile = iterator.next();
            if (persistenceController.getLocalFileEntity(localFile.toPath()) != null) {
                iterator.remove();
            }
        }
        return addedLocalFiles;
    }

    private Collection<File> getLocalAddeds(final Collection<File> localFiles, final ManagedMappings managedMappings) {
        final Collection<File> addedLocalFiles = new ArrayList<>(localFiles);
        final Collection<FileEntity> mappedExistingFiles = new ArrayList<>(managedMappings.getIdenticals());
        mappedExistingFiles.addAll(managedMappings.getUpdateds());
        mappedExistingFiles.addAll(managedMappings.getDeleteds());
        for (FileEntity mappedExistingFile : mappedExistingFiles) {

            addedLocalFiles.removeIf(file -> file.equals(new File(mappedExistingFile.getLocalPath())));
        }
        return addedLocalFiles;
    }

    private void getRemoteAddeds(AddedFiles addedFiles, Map<String, Collection<ServerEntry>> remoteFileLists, ManagedMappings managedMappings) {
        final Collection<FileEntity> mappedExistingFiles = new ArrayList<>(managedMappings.getIdenticals());
        mappedExistingFiles.addAll(managedMappings.getUpdateds());
        for (Map.Entry<String, Collection<ServerEntry>> remoteFiles : remoteFileLists.entrySet()) {
            final Collection<ServerEntry> addedRemoteFiles = new ArrayList<>(remoteFiles.getValue());
            final Iterator<ServerEntry> iterator = addedRemoteFiles.iterator();
            while (iterator.hasNext()) {
                ServerEntry serverEntry = iterator.next();
                for (FileEntity mappedExistingFile : mappedExistingFiles) {
                    if (mappedExistingFile.getAccountName().equals(serverEntry.getAccount()) && serverEntry.getPath().equals(Paths.get(mappedExistingFile.getRemotePath()))) {
                        iterator.remove();
                    }
                }
            }
            addedFiles.addRemote(remoteFiles.getKey(), addedRemoteFiles);
        }
    }

    private boolean isSameRevision(FileEntity mappedRemoteFile, ServerEntry serverEntry) {
        return serverEntry.getRevision().equals(mappedRemoteFile.getRevision());
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
