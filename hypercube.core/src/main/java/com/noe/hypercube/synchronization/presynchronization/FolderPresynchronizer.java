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
    private final Path targetFolder;

    @Inject
    private IPersistenceController persistenceController;
    @Inject
    private IAccountController accountController;

    public FolderPreSynchronizer(final Path targetFolder) {
        this.targetFolder = targetFolder;
    }

    @Override
    public void run() {
        final Map<String, Collection<ServerEntry>> remoteFileLists = createRemoteFileList();
        final Collection<File> localFiles = FileUtils.listFiles(targetFolder.toFile(), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        final Map<String, List<FileEntity>> files = persistenceController.getMappedEntities(targetFolder.toString());

        for (String localFilePath : files.keySet()) {
            final File mappedLocalFile = Paths.get(localFilePath).toFile();
            final List<FileEntity> mappings = files.get(localFilePath);
            // local file exist?
            final Map<Action, Collection<FileEntity>> sort = sort(mappings, remoteFileLists);
            final Collection<FileEntity> updated = sort.get(Action.CHANGED);
            // local file exist scenarios
            if(localFiles.contains(mappedLocalFile)) {
                final Collection<FileEntity> identicals = sort.get(Action.IDENTICAL);
                if(isLocalFileChanged(mappedLocalFile, identicals)) {
                    // local file changed scenarios
                    if (updated.isEmpty()) {

                    } else {

                    }
                }
                // local file is identical scenarios
                else {
                    // no updates
                    if (updated.isEmpty()) {
                        final Collection<FileEntity> deleted = sort.get(Action.REMOVED);
                        if (deleted.isEmpty()) {
                            // all identical
                            LOG.info("All remote spaces are identical for {}", mappedLocalFile.getName());
                        } else {
                            // delete identical files - all remaining -> there are no updated
                            deleteLocalFile(mappedLocalFile);
                            for (FileEntity identical : identicals) {
                                deleteRemote(identical);
                            }
                        }
                        // single update found
                    } else if (updated.size() == 1) {
                        final FileEntity updatedRemoteFile = updated.iterator().next();
                        final Collection<FileEntity> deleted = sort.get(Action.REMOVED);
                        if (deleted.isEmpty()) {
                            // all files are identical except the updated one -> update all
                            download(updatedRemoteFile);
                            uploadAllAccountsAsNew(updatedRemoteFile);
                        } else {
                            // upload updated file to the deleted ones remote space -> add + update all
                            download(updatedRemoteFile);
                            for (FileEntity deletedRemoteFile : deleted) {
                                uploadAsNew(mappedLocalFile, deletedRemoteFile);
                            }
                        }
                    } else {
                        // conflict - which update to choose?
                        // download all updated files  with resolved names
                        for (FileEntity updatedRemoteFile : updated) {
                            resolveFileName(updatedRemoteFile);
                            download(updatedRemoteFile);
                        }
                    }
                }
                // local file deleted scenario
            } else {
                final Collection<FileEntity> identicals = sort.get(Action.IDENTICAL);
                if (updated.isEmpty()) {
                    // no updates
                    if (!identicals.isEmpty()) {
                        // delete identical remote files
                        for (FileEntity identical : identicals) {
                            deleteRemote(identical);
                        }
                    }
                    // single update found
                } else if (updated.size() == 1) {
                    final FileEntity updatedRemoteFile = updated.iterator().next();
                    final Collection<FileEntity> deleted = sort.get(Action.REMOVED);
                    // download updated file -> restore deleted local file
                    download(updatedRemoteFile);
                    // all files are identical except the updated one -> update all
                    if (deleted.isEmpty()) {
                        // removed local file has been restored with the updated one - update identical remotes with it
                        for (FileEntity identical : identicals) {
                            updateAllAccounts(mappedLocalFile, identical);
                        }
                        // there are deleted remote files except the updated one
                    } else {
                        // upload updated file to the deleted remote spaces -> add + update all
                        for (FileEntity deletedRemoteFile : deleted) {
                            uploadAsNew(mappedLocalFile, deletedRemoteFile);
                        }
                        for (FileEntity identical : identicals) {
                            updateRemoteWith(mappedLocalFile, identical);
                        }
                    }
                    // conflict - resolve all names with account
                } else {
                    // TODO original updated files should be removed too - resolveFileName should create new Entity ????
                    // download all updated files  with resolved names
                    for (FileEntity updatedRemoteFile : updated) {
                        resolveFileName(updatedRemoteFile);
                        download(updatedRemoteFile);
                    }
                    // delete identical remote files
                    for (FileEntity identical : identicals) {
                        deleteRemote(identical);
                    }
                    // upload resolved for all remote drives
                    for (FileEntity resolvedRemoteFile : updated) {
                        uploadAllAccountsAsNew(resolvedRemoteFile);
                    }
                }
            }
        }
    }

    private Map<String, Collection<ServerEntry>> createRemoteFileList() {
        final Map<String, Collection<ServerEntry>> remoteFileLists = new HashMap<>();
        final Collection<AccountBox> accountBoxes = accountController.getAll();
        for (AccountBox accountBox : accountBoxes) {
            try {
                final Path remoteFolder = persistenceController.getRemoteFolder(accountBox.getClient().getMappingType(), targetFolder);
                if(remoteFolder != null) {
                    // local folder is mapped for account
                    final List<ServerEntry> fileList = accountBox.getClient().getFileList(remoteFolder);
                    remoteFileLists.put(accountBox.getAccountType().getName(), fileList);
                }
            } catch (SynchronizationException e) {
                e.printStackTrace();
            }
        }
        return remoteFileLists;
    }

    private boolean isLocalFileChanged(File mappedLocalFile, Collection<FileEntity> identicals) {
        try {
            final Long currentLocalFileCrc = FileUtils.checksumCRC32(mappedLocalFile);
            final LocalFileEntity storedLocalFileEntity = persistenceController.getLocalFileEntity(mappedLocalFile.toPath());
            final Long lastSyncedLocalFileCbc = storedLocalFileEntity.getCrc();
            if(!lastSyncedLocalFileCbc.equals(currentLocalFileCrc)) {
                return true;
            }
        } catch (IOException e) {
            return true;
        }
        return false;
    }

    private void updateAllAccounts(File mappedLocalFile, FileEntity fileEntity) {
        try {
            final UploadEntity uploadEntity = new UploadEntity(mappedLocalFile, Paths.get(fileEntity.getRemotePath()), Action.CHANGED);
            final Collection<AccountBox> accountBoxes = accountController.getAll();
            for (AccountBox accountBox : accountBoxes) {
                accountBox.getUploader().uploadUpdated(uploadEntity);
            }
        } catch (SynchronizationException e) {
            e.printStackTrace();
        }
    }

    private void uploadAsNew(File mappedLocalFile, FileEntity deletedRemoteFile) {
        try {
            final UploadEntity uploadEntity = new UploadEntity(mappedLocalFile, Paths.get(deletedRemoteFile.getRemotePath()), Action.ADDED);
            accountController.getAccountBox(deletedRemoteFile.getAccountName()).getUploader().uploadUpdated(uploadEntity);
        } catch (SynchronizationException e) {
            e.printStackTrace();
        }
    }

    private void uploadAllAccountsAsNew(FileEntity remoteFile) {
        try {
            final UploadEntity uploadEntity = new UploadEntity(new File(remoteFile.getLocalPath()), Paths.get(remoteFile.getRemotePath()), Action.ADDED);
            final Collection<AccountBox> accountBoxes = accountController.getAll();
            for (AccountBox accountBox : accountBoxes) {
                accountBox.getUploader().uploadNew(uploadEntity);
            }
        } catch (SynchronizationException e) {
            e.printStackTrace();
        }
    }

    private void download(FileEntity remoteFile) {
        final AccountBox accountBox = accountController.getAccountBox(remoteFile.getAccountName());
        try {
            final Path remotePath = Paths.get(remoteFile.getRemotePath());
            final Path localFolder = Paths.get(remoteFile.getLocalPath()).getParent();
            accountBox.getDownloader().download(remotePath, localFolder);
        } catch (SynchronizationException e) {
            e.printStackTrace();
        }
    }

    private void updateRemoteWith(File mappedLocalFile, FileEntity identical) {
        try {
            final UploadEntity uploadEntity = new UploadEntity(mappedLocalFile, Paths.get(identical.getRemotePath()), Action.CHANGED);
            accountController.getAccountBox(identical.getAccountName()).getUploader().uploadUpdated(uploadEntity);
        } catch (SynchronizationException e) {
            e.printStackTrace();
        }
    }

    private void deleteRemote(final FileEntity identical) {
        try {
            final UploadEntity uploadEntity = new UploadEntity(new File(identical.getLocalPath()), Paths.get(identical.getRemotePath()), Action.REMOVED);
            accountController.getAccountBox(identical.getAccountName()).getUploader().delete(uploadEntity);
        } catch (SynchronizationException e) {
            e.printStackTrace();
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
                    if(serverEntry.getRevision().equals(mapping.getRevision())) {
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
            if(map.containsKey(mappedFile.getLocalPath())) {
                map.get(mappedFile.getLocalPath()).add(mappedFile);
            }
        }
        return map;
    }

    private void deleteLocalFile(File mappedLocalFile) {
        try {
            FileUtils.forceDelete(mappedLocalFile);
        } catch (IOException e) {
            e.printStackTrace();
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
