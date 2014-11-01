package com.noe.hypercube.event;

import com.noe.hypercube.event.domain.request.*;

public interface FileEventHandler {

    void onFileListRequest(final FileListRequest event);

    void onUploadRequest(final UploadRequest event);

    void onDownloadRequest(final DownloadRequest event);

    void onCreateFolderRequest(final CreateFolderRequest event);

    void onDeleteRequest(final DeleteRequest event);
}
