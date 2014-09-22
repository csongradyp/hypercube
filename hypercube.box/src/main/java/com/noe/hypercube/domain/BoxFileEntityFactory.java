package com.noe.hypercube.domain;

import com.noe.hypercube.service.Box;

import java.util.Date;

public class BoxFileEntityFactory implements FileEntityFactory<Box, BoxFileEntity> {

    @Override
    public FileEntity createFileEntity(final String localPath, final String revision, final Date date) {
        return new BoxFileEntity(localPath, revision, date);
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
