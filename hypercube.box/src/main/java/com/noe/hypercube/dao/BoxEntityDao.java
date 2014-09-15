package com.noe.hypercube.dao;


import com.noe.hypercube.domain.BoxFileEntity;
import org.springframework.stereotype.Repository;

@Repository
public class BoxEntityDao extends EntityDao<BoxFileEntity> {

    @Override
    public Class<BoxFileEntity> getEntityClass() {
        return BoxFileEntity.class;
    }

}
