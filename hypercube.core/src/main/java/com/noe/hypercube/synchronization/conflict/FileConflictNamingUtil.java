package com.noe.hypercube.synchronization.conflict;

import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.domain.UploadEntity;
import com.noe.hypercube.util.DateUtil;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public final class FileConflictNamingUtil {

    private FileConflictNamingUtil() {
    }

    public static void resolveFileName(final FileEntity conflictedFile) {
        final Path remotePath = Paths.get(conflictedFile.getAccountName());
        final String baseName = FilenameUtils.getBaseName(remotePath.toString());
        final String ext = FilenameUtils.getExtension(remotePath.toString());
        final String currentDate = DateUtil.format(new Date());
        final String resolvedFileName = String.format("%s (%s %s).%s", baseName, conflictedFile.getAccountName(), currentDate, ext);
        conflictedFile.setRemotePath(Paths.get(remotePath.getParent().toString(), resolvedFileName).toString());
    }
    public static String createResolvedFileName(final UploadEntity uploadEntity) {
        final File file = uploadEntity.getFile();
        final String ext = FilenameUtils.getExtension(file.toString());
        final String baseName = FilenameUtils.getBaseName(file.toString());
        final String currentDate = DateUtil.format(new Date());
        return String.format("%s (%s %s).%s", baseName, uploadEntity.getOrigin(), currentDate, ext);
    }

}
