package com.noe.hypercube.dao;

import com.noe.hypercube.domain.BoxFileEntity;

public class BoxEntityDao extends Dao<BoxFileEntity> {

    @Override
    public Class<BoxFileEntity> getEntityClass() {
        return BoxFileEntity.class;
    }

}
