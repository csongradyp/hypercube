package com.noe.hypercube.dao.repository;

import com.noe.hypercube.domain.DriveFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriveEntityRepository extends JpaRepository<DriveFileEntity, String> {
}
