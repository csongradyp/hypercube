package com.noe.hypercube.mapping;

import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.domain.MappingEntity;
import com.noe.hypercube.mapping.collector.Collector;
import com.noe.hypercube.mapping.collector.LocalDirectoryCollector;
import com.noe.hypercube.mapping.collector.RemoteDirectoryCollector;
import com.noe.hypercube.service.Account;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static com.noe.hypercube.converter.DirectoryConverter.convertToLocalPath;
import static com.noe.hypercube.converter.DirectoryConverter.convertToRemotePath;

public abstract class DirectoryMapper<ACCOUNT_TYPE extends Account, MAPPING_TYPE extends MappingEntity> implements IMapper<ACCOUNT_TYPE, MAPPING_TYPE> {

    @Inject
    private IPersistenceController persistenceController;
    @Inject
    private LocalDirectoryCollector localDirectoryCollector;
    @Inject
    private RemoteDirectoryCollector remoteDirectoryCollector;

    @Override
    public List<Path> getLocals(final String remotePath) {
        Path path = Paths.get(remotePath);
        return getLocals(path);
    }

    @Override
    public List<Path> getLocals(final Path remotePath) {
        List<MappingEntity> mappedDirectories = getMappedDirectories(remotePath, localDirectoryCollector);
        List<Path> localDirs = new ArrayList<>();
        for (MappingEntity mapping : mappedDirectories) {
            Path localDir = convertToLocalPath(remotePath.getParent(), mapping);
            localDirs.add(localDir);
        }
        return localDirs;
    }

    @Override
    public List<Path> getRemotes(final File localPath) {
        final Path path = localPath.toPath();
        return getRemotes(path);
    }

    @Override
    public List<Path> getRemotes(final Path localPath) {
        List<MappingEntity> mappedDirectories = getMappedDirectories(localPath, remoteDirectoryCollector);
        List<Path> remoteDirs = new ArrayList<>();
        for (MappingEntity mapping : mappedDirectories) {
            Path remoteDir = convertToRemotePath(localPath.getParent(), mapping);
            remoteDirs.add(remoteDir);
        }
        return remoteDirs;
    }

    private List<MappingEntity> getMappedDirectories(final Path filePath, final Collector directoryCollector) {
        final Collection<MappingEntity> mappings = persistenceController.getMappings(getMappingClass());
        final List<MappingEntity> matchedMappings = new LinkedList<>();
        final String fileName = filePath.getFileName().toString();
        Path directory = filePath.normalize().getParent();
        while (directory != null) {
            Collection<MappingEntity> matchedRemoteDirs = directoryCollector.collect(directory, fileName, mappings);
            matchedMappings.addAll(matchedRemoteDirs);
            directory = directory.getParent();
        }
        return matchedMappings;
    }

    public void setPersistenceController(final IPersistenceController persistenceController) {
        this.persistenceController = persistenceController;
    }

    public void setLocalDirectoryCollector(final LocalDirectoryCollector localDirectoryCollector) {
        this.localDirectoryCollector = localDirectoryCollector;
    }

    public void setRemoteDirectoryCollector(final RemoteDirectoryCollector remoteDirectoryCollector) {
        this.remoteDirectoryCollector = remoteDirectoryCollector;
    }
}
