package com.noe.hypercube.event.domain;

import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.event.dto.RemoteQuotaInfo;

import java.nio.file.Path;
import java.util.List;

public class FileListResponse {

    private final List<ServerEntry> fileList;
    private final Path folder;
    private final String account;
    private final RemoteQuotaInfo quotaInfo;
    private Path parentFolder;

    public FileListResponse(final String account, final Path folder, List<ServerEntry> fileList, final RemoteQuotaInfo quotaInfo) {
        this.account = account;
        this.folder = folder;
        this.fileList = fileList;
        this.quotaInfo = quotaInfo;
    }

    public FileListResponse(String account, Path parentFolder, Path folder, List<ServerEntry> fileList, final RemoteQuotaInfo quotaInfo) {
        this(account, folder, fileList, quotaInfo);
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

    public RemoteQuotaInfo getQuotaInfo() {
        return quotaInfo;
    }
}
