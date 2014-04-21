package com.noe.hypercube.synchronization.downstream;


import com.noe.hypercube.domain.DbxFileEntity;
import com.noe.hypercube.domain.FileEntity;
import com.noe.hypercube.domain.MappingEntity;
import com.noe.hypercube.mapping.DirectoryMapper;
import com.noe.hypercube.service.IClient;

import java.util.Date;

public class DbxDownstreamSynchTask extends DefaultDownstreamSynchronizer {

    public DbxDownstreamSynchTask(IClient client, DirectoryMapper<MappingEntity> directoryMapper) {
        super(client, directoryMapper);
    }

    @Override
    protected FileEntity createFileEntity(String localPath, String revision, Date date) {
        return new DbxFileEntity(localPath, revision, date);
    }
}
