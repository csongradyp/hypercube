package com.noe.hypercube.dao.repository;

import com.noe.hypercube.domain.BoxFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoxEntityRepository extends JpaRepository<BoxFileEntity, String> {
}
