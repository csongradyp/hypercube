package com.noe.hypercube.mapping;

import com.noe.hypercube.controller.MappingController;
import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.domain.MappingEntity;
import com.noe.hypercube.mapping.collector.Collector;
import com.noe.hypercube.mapping.collector.LocalDirectoryCollector;
import com.noe.hypercube.mapping.collector.RemoteDirectoryCollector;

import javax.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static com.noe.hypercube.converter.DirectoryConverter.convertToLocalPath;
import static com.noe.hypercube.converter.DirectoryConverter.convertToRemotePath;

public abstract class DirectoryMapper<MAPPING_TYPE extends MappingEntity, ENTITY_TYPE extends FileEntity> {

    private MappingController mappingController;
    @Inject
    private LocalDirectoryCollector localDirectoryCollector;
    @Inject
    private RemoteDirectoryCollector remoteDirectoryCollector;

    public abstract Class<MAPPING_TYPE> getMappingClass();
    public abstract Class<ENTITY_TYPE> getEntityClass();

    public List<Path> getLocals(final String remotePath) {
        Path path = Paths.get(remotePath);
        return getLocals(path);
    }

    public List<Path> getLocals(final Path remotePath) {
        List<MappingEntity> mappedDirectories = getMappedDirectories(remotePath, localDirectoryCollector);
        List<Path> localDirs = new LinkedList<>();
        for (MappingEntity mapping : mappedDirectories) {
            Path localDir = convertToLocalPath(remotePath.getParent(), mapping);
            localDirs.add(localDir);
        }
        return localDirs;
    }

    public List<Path> getRemotes(final String localPath) {
        final Path path = Paths.get(localPath);
        return getRemotes(path);
    }

    public List<Path> getRemotes(final Path localPath) {
        List<MappingEntity> mappedDirectories = getMappedDirectories(localPath, remoteDirectoryCollector);
        List<Path> remoteDirs = new LinkedList<>();
        for (MappingEntity mapping : mappedDirectories) {
            Path remoteDir = convertToRemotePath(localPath.getParent(), mapping);
            remoteDirs.add(remoteDir);
        }
        return remoteDirs;

    }

    private List<MappingEntity> getMappedDirectories(final Path filePath, final Collector directoryCollector) {
        final Collection<MappingEntity> mappings = mappingController.getMappings(getMappingClass());
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

    public void setMappingController(final MappingController mappingController) {
        this.mappingController = mappingController;
    }

    public void setLocalDirectoryCollector(final LocalDirectoryCollector localDirectoryCollector) {
        this.localDirectoryCollector = localDirectoryCollector;
    }

    public void setRemoteDirectoryCollector(final RemoteDirectoryCollector remoteDirectoryCollector) {
        this.remoteDirectoryCollector = remoteDirectoryCollector;
    }
}
