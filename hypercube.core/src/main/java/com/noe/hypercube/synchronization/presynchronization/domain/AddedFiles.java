package com.noe.hypercube.synchronization.presynchronization.domain;

import com.noe.hypercube.domain.ServerEntry;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AddedFiles {

    private static final Logger LOG = LoggerFactory.getLogger(AddedFiles.class);

    private final Collection<File> locals;
    private final Collection<File> localConflicteds;
    private final Map<String, Collection<ServerEntry>> remotes;
    private final Map<String, Collection<ServerEntry>> remoteConflicteds;

    public AddedFiles() {
        locals = new ArrayList<>();
        localConflicteds = new ArrayList<>();
        remotes = new HashMap<>();
        remoteConflicteds = new HashMap<>();
    }

    public void checkAndResolveConflicts() {
        for (File localFile : locals) {
            for (String account : remotes.keySet()) {
                final Collection<ServerEntry> accountFiles = remotes.get(account);
                final ArrayList<ServerEntry> remoteConflicted = new ArrayList<>();
                for (ServerEntry accountFile : accountFiles) {
                    final Path localPath = localFile.toPath();
                    final Path remotePath = accountFile.getPath();
                    if (FilenameUtils.equalsNormalized(localPath.getFileName().toString(), remotePath.getFileName().toString())) {
                        LOG.info("PreSynchronization - Added file conflict founded: {} with account {} file {}", localPath, account, remotePath);
                        localConflicteds.add(localFile);
                        remoteConflicted.add(accountFile);
                    }
                }
                remoteConflicteds.put(account, remoteConflicted);
                accountFiles.removeAll(remoteConflicted);
            }
        }
        locals.removeAll(localConflicteds);
    }

    public void addRemote(final String account, final Collection<ServerEntry> remoteFiles) {
        remotes.put(account, remoteFiles);
    }

    public void addLocals(Collection<File> addedLocalFiles) {
        locals.addAll(addedLocalFiles);
    }

    public Collection<File> getLocals() {
        return locals;
    }

    public Map<String, Collection<ServerEntry>> getRemotes() {
        return remotes;
    }

    public Collection<File> getLocalConflicteds() {
        return localConflicteds;
    }

    public Map<String, Collection<ServerEntry>> getRemoteConflicteds() {
        return remoteConflicteds;
    }
}
