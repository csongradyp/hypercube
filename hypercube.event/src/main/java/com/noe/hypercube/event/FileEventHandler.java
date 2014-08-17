package com.noe.hypercube.event;

import com.noe.hypercube.event.domain.CreateFolderRequest;
import com.noe.hypercube.event.domain.DownloadRequest;
import com.noe.hypercube.event.domain.FileListRequest;
import com.noe.hypercube.event.domain.UploadRequest;

public interface FileEventHandler {

    void onFileListRequest(final FileListRequest event);

    void onUploadRequest(final UploadRequest event);

    void onDownloadRequest(final DownloadRequest event);

    void onCreateFolderRequest(final CreateFolderRequest event);
}
