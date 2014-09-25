package com.noe.hypercube.synchronization.presynchronization;

import com.noe.hypercube.controller.IAccountController;
import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.domain.AccountBox;
import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.observer.local.LocalFileListener;
import com.noe.hypercube.synchronization.Action;
import com.noe.hypercube.synchronization.SynchronizationException;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class FolderPresynchronizer {

    @Inject
    private IPersistenceController persistenceController;
    @Inject
    private IAccountController accountController;

    public void run(final File localFolder, final LocalFileListener localFileListener, final Map<String, Collection<ServerEntry>> remoteFileLists) {
        final Collection<File> localFiles = FileUtils.listFiles(localFolder, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        final Map<String, List<FileEntity>> files = persistenceController.getMappedEntities(localFolder.toPath().toString());

        for (String localFilePath : files.keySet()) {
            final File mappedLocalFile = Paths.get(localFilePath).toFile();
            final List<FileEntity> mappings = files.get(localFilePath);
            // local file exist?
            final Map<Action, Collection<FileEntity>> sort = sort(mappings, remoteFileLists);
            if(localFiles.contains(mappedLocalFile)) {
                // local file exist scenarios
                final Collection<FileEntity> updated = sort.get(Action.CHANGED);
                if (updated.isEmpty()) {
                    // no updates
                    final Collection<FileEntity> deleted = sort.get(Action.REMOVED);
                    if (deleted.isEmpty() && !sort.get(Action.IDENTICAL).isEmpty()) {
                        // nothing to do -> log
                    } else {
                        // delete identical files - all remaining -> there are no updated
                        localFileListener.onFileDelete(mappedLocalFile);
                        final Collection<FileEntity> identicals = sort.get(Action.IDENTICAL);
                        for (FileEntity identical : identicals) {
                            localFileListener.onFileDelete(Paths.get(identical.getLocalPath()).toFile());
                        }
                    }
                } else if (updated.size() == 1) {
                    // single update found
                    final Collection<FileEntity> deleted = sort.get(Action.REMOVED);
                    final FileEntity updatedRemoteFile = updated.iterator().next();
                    final AccountBox accountBox = accountController.getAccountBox(updatedRemoteFile.getAccountName());
                    if (deleted.isEmpty()) {
                        // all files are identical except the updated one -> update all
                        try {
                            accountBox.getDownloader().download(Paths.get(updatedRemoteFile.getRemotePath()), Paths.get(updatedRemoteFile.getLocalPath()).getParent());
                        } catch (SynchronizationException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // upload updated file to the deleted ones remote space -> add + update all
                        try {
                            accountBox.getDownloader().download(Paths.get(updatedRemoteFile.getRemotePath()), Paths.get(updatedRemoteFile.getLocalPath()).getParent());
                        } catch (SynchronizationException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    // conflict - which update to choose?
                }
            } else {
                // local file deleted scenario
            }
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

    private void delete(File mappedLocalFile) {
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


}
