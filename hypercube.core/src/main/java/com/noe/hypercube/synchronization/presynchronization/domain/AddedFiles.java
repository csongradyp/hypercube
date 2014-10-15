package com.noe.hypercube.synchronization.presynchronization.domain;

import com.noe.hypercube.domain.ServerEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AddedFiles {

    private final Collection<File> locals;
    private final Map<String, Collection<ServerEntry>> remotes;

    public AddedFiles() {
        locals = new ArrayList<>();
        remotes = new HashMap<>();
    }

    public void addRemote(final String account, final Collection<ServerEntry> remoteFiles) {
        remotes.put(account, remoteFiles);
    }

    public void addLocals(Collection<File> addedLocalFiles) {
        locals.addAll(addedLocalFiles);
    }
}
