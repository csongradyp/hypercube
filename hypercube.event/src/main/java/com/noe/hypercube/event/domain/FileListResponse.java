package com.noe.hypercube.event.domain;

import com.noe.hypercube.domain.ServerEntry;

import java.nio.file.Path;
import java.util.List;

public class FileListResponse {

    private final List<ServerEntry> fileList;
    private final Path folder;
    private final String account;

    public FileListResponse(String account, Path folder, List<ServerEntry> fileList) {
        this.account = account;
        this.folder = folder;
        this.fileList = fileList;
    }

    public List<ServerEntry> getFileList() {
        return fileList;
    }

    public Path getFolder() {
        return folder;
    }

    public String getAccount() {
        return account;
    }
}
