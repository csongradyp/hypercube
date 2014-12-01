package com.noe.hypercube.domain;

import com.noe.hypercube.persistence.FileEntityFactory;
import com.noe.hypercube.persistence.domain.FileEntity;
import com.noe.hypercube.service.Box;

import java.util.Date;

public class BoxFileEntityFactory implements FileEntityFactory<Box, BoxFileEntity> {

    @Override
    public FileEntity createFileEntity(final String localPath, final String remotePath, final String revision, final Date date) {
        return new BoxFileEntity(localPath, remotePath, revision, date);
    }

    @Override
    public Class<Box> getAccountType() {
        return Box.class;
    }

    @Override
    public Class<BoxFileEntity> getEntityType() {
        return BoxFileEntity.class;
    }
}
