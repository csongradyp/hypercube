package com.noe.hypercube.mapping;

import com.noe.hypercube.domain.MappingEntity;
import com.noe.hypercube.service.Account;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public interface IMapper<ACCOUNT_TYPE extends Account, MAPPING_TYPE extends MappingEntity> {
    Class<MAPPING_TYPE> getMappingClass();

    Class<ACCOUNT_TYPE> getAccountType();

    List<Path> getLocals(String remotePath);

    List<Path> getLocals(Path remotePath);

    List<Path> getRemotes(File localPath);

    List<Path> getRemotes(Path localPath);
}
