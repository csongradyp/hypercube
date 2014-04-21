package com.noe.hypercube.converter;

import com.noe.hypercube.domain.MappingEntity;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class DirectoryConverter {

    public static Path convertToRemotePath(Path localDir, MappingEntity mapping) {
        Path mappedRemoteDir = mapping.getRemoteDir();
        Path mappedLocalDir = mapping.getLocalDir();
        return addSubDirsTo(mappedRemoteDir, localDir, mappedLocalDir);
    }

    public static Path convertToLocalPath(Path remoteDir, MappingEntity mapping) {
        Path mappedLocalDir = mapping.getLocalDir();
        Path mappedRemoteDir = mapping.getRemoteDir();
        return addSubDirsTo(mappedLocalDir, remoteDir, mappedRemoteDir);
    }

    private static Path addSubDirsTo(Path dirToAmend, Path originalDir, Path mappedDir) {
        String subDirs = originalDir.toString().replace(mappedDir.toString(), "");
        return Paths.get(dirToAmend.toString() + subDirs);
    }

}
