package com.noe.hypercube.event.domain;

import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.event.dto.RemoteQuotaInfo;

import java.nio.file.Path;
import java.util.List;

public class FileListResponse {

    private final List<ServerEntry> fileList;
    private final Path folder;
    private final Path previousFolder;
    private final String account;
    private final RemoteQuotaInfo quotaInfo;
    private Boolean cloud;

    public FileListResponse(final String account, Path previousFolder, final Path folder, List<ServerEntry> fileList, final RemoteQuotaInfo quotaInfo) {
        this.account = account;
        this.previousFolder = previousFolder;
        this.folder = folder;
        this.fileList = fileList;
        this.quotaInfo = quotaInfo;
        cloud = false;
    }

    public List<ServerEntry> getFileList() {
        return fileList;
    }

    public Path getFolder() {
        return folder;
    }

    public Path getPreviousFolder() {
        return previousFolder;
    }

    public String getAccount() {
        return account;
    }

    public RemoteQuotaInfo getQuotaInfo() {
        return quotaInfo;
    }

    public void setCloud(Boolean cloud) {
        this.cloud = cloud;
    }

    public Boolean isCloud() {
        return cloud;
    }
}
