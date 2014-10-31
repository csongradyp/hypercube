package com.noe.hypercube.googledrive.dao.repository;

import com.noe.hypercube.googledrive.domain.DriveFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriveEntityRepository extends JpaRepository<DriveFileEntity, String> {
}
