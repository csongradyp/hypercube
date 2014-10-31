package com.noe.hypercube.ui.util;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class PathConverterUtil {

    public static String getAccount(final Path location) {
        return location.getName(0).toString();
    }

    public static Path getEventPath(final Path path) {
        if (path == null || path.getNameCount() < 2) {
            return Paths.get("");
        }
        return path.subpath(1, path.getNameCount());
    }

}
