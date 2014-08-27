package com.noe.hypercube.event.domain;

import com.noe.hypercube.domain.ServerEntry;

import java.nio.file.Path;
import java.util.List;

public class FileListResponse {

    private final List<ServerEntry> fileList;
    private final Path folder;
    private Path parentFolder;
    private final String account;

    public FileListResponse(String account, Path folder, List<ServerEntry> fileList) {
        this.account = account;
        this.folder = folder;
        this.fileList = fileList;
    }

    public FileListResponse(String account, Path parentFolder, Path folder, List<ServerEntry> fileList) {
        this(account, folder, fileList);
        this.parentFolder = parentFolder;
    }

    public List<ServerEntry> getFileList() {
        return fileList;
    }

    public Path getFolder() {
        return folder;
    }

    public Path getParentFolder() {
        return parentFolder;
    }

    public String getAccount() {
        return account;
    }
}
