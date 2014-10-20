package com.noe.hypercube.googledrive.dao.repository;

import com.noe.hypercube.googledrive.domain.DriveMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriveMappingRepository extends JpaRepository<DriveMapping, String> {
}
