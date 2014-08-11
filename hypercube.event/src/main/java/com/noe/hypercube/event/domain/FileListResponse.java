package com.noe.hypercube.event.domain;

import com.noe.hypercube.domain.ServerEntry;

import java.nio.file.Path;
import java.util.List;

public class FileListResponse {

    private final List<ServerEntry> fileList;
    private final Path parentFolder;
    private final String account;

    public FileListResponse(String account, Path parentFolder, List<ServerEntry> fileList) {
        this.account = account;
        this.parentFolder = parentFolder;
        this.fileList = fileList;
    }

    public List<ServerEntry> getFileList() {
        return fileList;
    }

    public Path getParentFolder() {
        return parentFolder;
    }

    public String getAccount() {
        return account;
    }
}
