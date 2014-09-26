package com.noe.hypercube.event.domain;

import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.event.dto.RemoteQuotaInfo;

import java.nio.file.Path;
import java.util.List;

public class FileListResponse extends AccountActionEvent {

    private final List<ServerEntry> fileList;
    private final Path folder;
    private final Path previousFolder;
    private final RemoteQuotaInfo quotaInfo;
    private Boolean cloud;

    public FileListResponse(final String account, final Path previousFolder, final Path folder, List<ServerEntry> fileList, final RemoteQuotaInfo quotaInfo) {
        super(account);
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
