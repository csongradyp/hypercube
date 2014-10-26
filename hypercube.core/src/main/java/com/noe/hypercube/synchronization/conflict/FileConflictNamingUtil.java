package com.noe.hypercube.synchronization.conflict;

import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.domain.UploadEntity;
import com.noe.hypercube.util.DateUtil;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

public final class FileConflictNamingUtil {

    private FileConflictNamingUtil() {
    }

    public static void resolveFileName(final FileEntity conflictedFile) {
        final Path remotePath = Paths.get(conflictedFile.getRemotePath());
        final String baseName = FilenameUtils.getBaseName(remotePath.toString());
        final String ext = FilenameUtils.getExtension(remotePath.toString());
        final String currentDate = DateUtil.fileNameCompatibleFormat(new Date());
        final String resolvedFileName = String.format("%s (%s %s).%s", baseName, conflictedFile.getAccountName(), currentDate, ext);
        conflictedFile.setRemotePath(Paths.get(remotePath.getParent().toString(), resolvedFileName).toString());
    }

    public static String getResolvedFileName(final FileEntity conflictedFile) {
        final Path remotePath = Paths.get(conflictedFile.getRemotePath());
        final String baseName = FilenameUtils.getBaseName(remotePath.toString());
        final String ext = FilenameUtils.getExtension(remotePath.toString());
        final String currentDate = DateUtil.fileNameCompatibleFormat(new Date());
        return String.format("%s (%s %s).%s", baseName, conflictedFile.getAccountName(), currentDate, ext);
    }

    public static String getResolvedFileName(final Path conflictedFile) {
        final String baseName = FilenameUtils.getBaseName(conflictedFile.toString());
        final String ext = FilenameUtils.getExtension(conflictedFile.toString());
        final String currentDate = DateUtil.fileNameCompatibleFormat(new Date());
        return String.format("%s (%s).%s", baseName, currentDate, ext);
    }

    public static Path getResolveFilePath(final Path conflictedFile) {
        final String resolvedFileName = getResolvedFileName(conflictedFile);
        return Paths.get(conflictedFile.getParent().toString(), resolvedFileName);
    }

    public static FileEntity resolveFile(final FileEntity conflictedFile) {
        final Path remotePath = Paths.get(conflictedFile.getRemotePath());
        final String baseName = FilenameUtils.getBaseName(remotePath.toString());
        final String ext = FilenameUtils.getExtension(remotePath.toString());
        final String currentDate = DateUtil.fileNameCompatibleFormat(new Date());
        final String resolvedFileName = String.format("%s (%s %s).%s", baseName, conflictedFile.getAccountName(), currentDate, ext);
        final FileEntity resolved = conflictedFile.duplicate();
        resolved.setRemotePath(Paths.get(remotePath.getParent().toString(), resolvedFileName).toString());
        return resolved;
    }

    public static Collection<FileEntity> createResolvedCopies(final Collection<FileEntity> conflictedFiles) {
        return conflictedFiles.stream().map(FileConflictNamingUtil::resolveFile).collect(Collectors.toList());
    }

    public static String resolveFileName(final Path remotePath, final String accountName) {
        final String baseName = FilenameUtils.getBaseName(remotePath.toString());
        final String ext = FilenameUtils.getExtension(remotePath.toString());
        final String currentDate = DateUtil.fileNameCompatibleFormat(new Date());
        return String.format("%s (%s %s).%s", baseName, accountName, currentDate, ext);
    }

    public static String createResolvedFileName(final UploadEntity uploadEntity) {
        final File file = uploadEntity.getFile();
        final String ext = FilenameUtils.getExtension(file.toString());
        final String baseName = FilenameUtils.getBaseName(file.toString());
        final String currentDate = DateUtil.fileNameCompatibleFormat(new Date());
        return String.format("%s (%s %s).%s", baseName, uploadEntity.getOrigin(), currentDate, ext);
    }

}
