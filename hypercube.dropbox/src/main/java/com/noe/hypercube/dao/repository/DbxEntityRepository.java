package com.noe.hypercube.dao.repository;

import com.noe.hypercube.domain.DbxFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DbxEntityRepository extends JpaRepository<DbxFileEntity, String> {
}
