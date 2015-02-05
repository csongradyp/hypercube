package com.noe.hypercube.event.domain.request;

import java.nio.file.Path;

public interface IFileListRequest {

    Path getFolder();

    Boolean isCloud();

    Integer getTarget();
}
