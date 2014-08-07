package com.noe.hypercube.event.domain;

import com.noe.hypercube.event.dto.RemoteFileInfo;

import java.util.List;

public class FileListResponse {

    private final List<RemoteFileInfo> fileList;
    private final String account;

    public FileListResponse( String account, List<RemoteFileInfo> fileList ) {
        this.account = account;
        this.fileList = fileList;
    }

    public List<RemoteFileInfo> getFileList() {
        return fileList;
    }

    public String getAccount() {
        return account;
    }
}
