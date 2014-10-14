package com.noe.hypercube.synchronization.presynchronization;

import com.noe.hypercube.domain.FileEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ManagedMappings {

    private final Collection<FileEntity> updateds;
    private final Collection<FileEntity> deleteds;
    private final Collection<FileEntity> identicals;

    public ManagedMappings(final List<FileEntity> mappedRemoteFiles) {
        identicals = new ArrayList<>();
        updateds = new ArrayList<>();
        deleteds = new ArrayList<>(mappedRemoteFiles);
    }

    public void addUpdated(final FileEntity fileEntity) {
        updateds.add(fileEntity);
        deleteds.remove(fileEntity);
    }

    public void addIdentical(final FileEntity fileEntity) {
        identicals.add(fileEntity);
        deleteds.remove(fileEntity);
    }

    public Collection<FileEntity> getUpdateds() {
        return updateds;
    }

    public Collection<FileEntity> getDeleteds() {
        return deleteds;
    }

    public Collection<FileEntity> getIdenticals() {
        return identicals;
    }
}
