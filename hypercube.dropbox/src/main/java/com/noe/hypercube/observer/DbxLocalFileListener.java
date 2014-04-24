package com.noe.hypercube.observer;

import com.noe.hypercube.domain.MappingEntity;
import com.noe.hypercube.mapping.DirectoryMapper;
import com.noe.hypercube.service.Dropbox;
import com.noe.hypercube.synchronization.upstream.IUploader;

public class DbxLocalFileListener extends LocalFileListener<Dropbox> {

    public DbxLocalFileListener(IUploader<Dropbox> uploader, DirectoryMapper<? extends MappingEntity, Dropbox> mapper) {
        super(uploader, mapper);
    }
}
