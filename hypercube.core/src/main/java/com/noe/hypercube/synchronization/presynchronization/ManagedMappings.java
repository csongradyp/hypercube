package com.noe.hypercube.synchronization.presynchronization;

import com.noe.hypercube.domain.FileEntity;

import java.util.ArrayList;
import java.util.Collection;

public class ManagedMappings {

    private final Collection<FileEntity> updated;
    private final Collection<FileEntity> deleted;
    private final Collection<FileEntity> identical;

    public ManagedMappings() {
        updated = new ArrayList<>();
        deleted = new ArrayList<>();
        identical = new ArrayList<>();
    }

    public void addUpdated(FileEntity fileEntity) {
        updated.add(fileEntity);
    }

    public void addDeleted(FileEntity fileEntity) {
        updated.add(fileEntity);
    }

    public void addIdentical(FileEntity fileEntity) {
        updated.add(fileEntity);
    }

    public Collection<FileEntity> getUpdated() {
        return updated;
    }

    public Collection<FileEntity> getDeleted() {
        return deleted;
    }

    public Collection<FileEntity> getIdentical() {
        return identical;
    }
}
