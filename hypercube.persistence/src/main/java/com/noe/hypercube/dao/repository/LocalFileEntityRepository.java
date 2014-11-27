package com.noe.hypercube.dao.repository;

import com.noe.hypercube.persistence.domain.LocalFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocalFileEntityRepository extends JpaRepository<LocalFileEntity, String> {
}
