package com.noe.hypercube.persistence;

import com.noe.hypercube.persistence.domain.AccountEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IAccountPersistenceController extends JpaRepository<AccountEntity, Integer> {

    Optional<AccountEntity> findByAccountName(String accountName);

    List<AccountEntity> findByAttachedTrue();

    List<AccountEntity> findByAttachedFalse();

}
