package com.noe.hypercube.converter;

import com.noe.hypercube.persistence.domain.MappingEntity;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class DirectoryConverter {

    public static Path convertToRemotePath(Path localDir, MappingEntity mapping) {
        Path mappedRemoteDir = Paths.get(mapping.getRemoteDir());
        Path mappedLocalDir = Paths.get(mapping.getLocalDir());
        return addSubDirsTo(mappedRemoteDir, localDir, mappedLocalDir);
    }

    public static Path convertToLocalPath(Path remoteDir, MappingEntity mapping) {
        Path mappedLocalDir = Paths.get(mapping.getLocalDir());
        Path mappedRemoteDir = Paths.get(mapping.getRemoteDir());
        return addSubDirsTo(mappedLocalDir, remoteDir, mappedRemoteDir);
    }

    private static Path addSubDirsTo(Path dirToAmend, Path originalDir, Path mappedDir) {
        String subDirs = originalDir.toString().replace(mappedDir.toString(), "");
        return Paths.get(dirToAmend.toString() + subDirs);
    }

}
