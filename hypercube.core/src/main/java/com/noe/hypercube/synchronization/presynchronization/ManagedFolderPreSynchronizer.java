package com.noe.hypercube.synchronization.presynchronization;

import com.noe.hypercube.controller.IAccountController;
import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.domain.*;
import com.noe.hypercube.synchronization.SynchronizationException;
import com.noe.hypercube.synchronization.conflict.FileConflictNamingUtil;
import com.noe.hypercube.synchronization.presynchronization.domain.AddedFiles;
import com.noe.hypercube.synchronization.presynchronization.domain.ManagedMappings;
import com.noe.hypercube.synchronization.presynchronization.util.PreSynchronizationSubmitManager;
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

    private static final Logger LOG = LoggerFactory.getLogger(ManagedFolderPreSynchronizer.class);

    private final IPersistenceController persistenceController;
    private final IAccountController accountController;
    private final PreSynchronizationSubmitManager submitManager;
    private final Set<AccountBox> accountBoxes;
    private final Path targetFolder;

    public ManagedFolderPreSynchronizer(final Path targetFolder, final IPersistenceController persistenceController, final IAccountController accountController, final PreSynchronizationSubmitManager submitManager) {
        this.targetFolder = targetFolder;
        this.persistenceController = persistenceController;
        this.accountController = accountController;
        this.submitManager = submitManager;
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

    private ManagedMappings getMappingsByAction(final List<FileEntity> mappedRemoteFiles, final Map<String, Collection<ServerEntry>> remoteFileLists) {
        final ManagedMappings managedMappings = new ManagedMappings(mappedRemoteFiles);
        // sort local file mappings by action
        for (FileEntity mappedRemoteFile : mappedRemoteFiles) {
            final String accountName = mappedRemoteFile.getAccountName();
            final Collection<ServerEntry> accountFileList = remoteFileLists.get(accountName);
            for (ServerEntry serverEntry : accountFileList) {
                if (serverEntry.getPath().equals(Paths.get(mappedRemoteFile.getRemotePath()))) {
                    if (isSameRevision(mappedRemoteFile, serverEntry)) {
                        managedMappings.addIdentical(mappedRemoteFile);
                    } else {
                        managedMappings.addUpdated(mappedRemoteFile);
                    }
                }
            }
        }
        return managedMappings;
    }

    private boolean isSameRevision(FileEntity mappedRemoteFile, ServerEntry serverEntry) {
        return serverEntry.getRevision().equals(mappedRemoteFile.getRevision());
    }

    private AddedFiles getAddedFiles(Map<String, Collection<ServerEntry>> remoteFileLists, Collection<File> localFiles, ManagedMappings sortedMappings) {
        final AddedFiles addedFiles = new AddedFiles();
        getRemoteAddeds(addedFiles, remoteFileLists, sortedMappings);
        final Collection<File> localAddeds = getLocalAddeds(localFiles);
        addedFiles.addLocals(localAddeds);
        return addedFiles;
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
                submitManager.delete(identicals);
            }
        } else if (updateds.size() == 1) {
            // single update found
            final FileEntity updatedRemoteFile = updateds.iterator().next();
            submitManager.download(updatedRemoteFile);
            if (deleteds.isEmpty()) {
                // all files are identical except the updated one -> update all
                // FIXME - local file must exist at this time - must wait the download
                submitManager.uploadAsNew(mappedLocalFile, updatedRemoteFile);
            } else {
                // upload updated file to the deleted ones remote space -> add + update all
                submitManager.uploadAsNew(mappedLocalFile, deleteds);
            }
        } else {
            // conflict - which update to choose?
            // download all updated files  with resolved names
            for (FileEntity updatedRemoteFile : updateds) {
                resolveFileName(updatedRemoteFile);
                submitManager.download(updatedRemoteFile);
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
                submitManager.uploadUpdated(mappedLocalFile, identicals);
            } else {
                // all remotes deleted - upload updated local file as new
                submitManager.uploadAllAccountsAsNew(mappedLocalFile, deleteds, accountBoxes);
            }
        } else {
            // CONFLICTED CASES - ALL
            // rename local file with resolved name -> upload
            final File resolvedLocalFileDestination = new File(FileConflictNamingUtil.getResolveFileName(mappedLocalFile));
            mappedLocalFile.renameTo(resolvedLocalFileDestination);
            // upload renamed local file to all accounts
            submitManager.uploadAllAccountsAsNew(resolvedLocalFileDestination, updateds, accountBoxes);
            submitManager.uploadAllAccountsAsNew(resolvedLocalFileDestination, identicals, accountBoxes);
            submitManager.uploadAllAccountsAsNew(resolvedLocalFileDestination, deleteds, accountBoxes);

            // there are updated remote files - rename remote files with new resolved name
            final Collection<FileEntity> renamedUpdatedRemotesWithResolvedName = renameRemotesWithResolvedName(updateds);
            // download renamed files
            submitManager.download(renamedUpdatedRemotesWithResolvedName);

            // there are identical remote files - rename remote files with new resolved name
//            final Collection<FileEntity> renamedIdenticalRemotesWithResolvedName = renameRemotesWithResolvedName(identicals);
            if (!identicals.isEmpty()) {
                // download renamed files
//                submitManager.download(renamedIdenticalRemotesWithResolvedName);
                // upload all renamed updated files to identical account stores
                for (FileEntity identical : identicals) {
                    submitManager.uploadAsNewResolvedTo(renamedUpdatedRemotesWithResolvedName, identical);
                }
            }
            if (!deleteds.isEmpty()) {
                // there are deleted remote files - upload downloaded resolved files
                // upload all renamed updated + identical files to deleted account stores
                for (FileEntity deleted : deleteds) {
//                    submitManager.uploadAsNewResolvedTo(renamedIdenticalRemotesWithResolvedName, deleted);
                    submitManager.uploadAsNewResolvedTo(renamedUpdatedRemotesWithResolvedName, deleted);
                }
            }
        }
    }

    private Collection<FileEntity> renameRemotesWithResolvedName(Collection<FileEntity> updateds) {
        Collection<FileEntity> renamedRemotesWithResolvedName = new ArrayList<>();
        try {
            renamedRemotesWithResolvedName = submitManager.renameRemoteWithResolvedName(updateds);
        } catch (SynchronizationException e) {
            LOG.error(e.getMessage(), e);
        }
        return renamedRemotesWithResolvedName;
    }

    private void localFileDeletedScenario(File mappedLocalFile, ManagedMappings sort) {
        final Collection<FileEntity> identicals = sort.getIdenticals();
        final Collection<FileEntity> updateds = sort.getUpdateds();
        final Collection<FileEntity> deleteds = sort.getDeleteds();
        if (updateds.isEmpty()) {
            // no updates
            if (deleteds.isEmpty()) {
                // delete identical remote files
                submitManager.delete(identicals);
            } else {
                // all files deleted - remote and local
            }
            // single update found
        } else if (updateds.size() == 1) {
            final FileEntity updatedRemoteFile = updateds.iterator().next();
            // download updated file -> restore deleted local file
            submitManager.download(updatedRemoteFile);
            // all files are identical except the updated one -> update all
            if (deleteds.isEmpty()) {
                // removed local file has been restored with the updated one - update identical remotes with it
                submitManager.updateFor(mappedLocalFile, identicals, accountBoxes);
                // there are deleted remote files except the updated one
            } else {
                // upload updated file as a new file to the deleted remote spaces
                submitManager.uploadAsNew(mappedLocalFile, deleteds);
                // upload updated file to the identical remote spaces
                submitManager.uploadUpdated(mappedLocalFile, identicals);
            }
            // conflict - resolve all names with account
        } else {
            // TODO original updated files should be removed too - resolveFileName should create new Entity ????
            // download all updated files  with resolved names
            final Collection<FileEntity> renamedUpdatedsRemotesWithResolvedName = renameRemotesWithResolvedName(updateds);
            submitManager.download(renamedUpdatedsRemotesWithResolvedName);
            // delete identical remote files
            submitManager.delete(identicals);
            // upload resolved for all remaining remote drives
            for (FileEntity identical : identicals) {
                submitManager.uploadAsNewResolvedTo(renamedUpdatedsRemotesWithResolvedName, identical);
            }
            for (FileEntity deleted : deleteds) {
                submitManager.uploadAsNewResolvedTo(renamedUpdatedsRemotesWithResolvedName, deleted);
            }
        }
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

    private void deleteLocalFile(File mappedLocalFile) {
        try {
            FileUtils.forceDelete(mappedLocalFile);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public Path getTargetFolder() {
        return targetFolder;
    }
}
