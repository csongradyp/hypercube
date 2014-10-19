package com.noe.hypercube.event.domain;

import java.nio.file.Path;

public class MappingResponse extends MappingEvent {

    public MappingResponse(final Path localFolder) {
        super(localFolder);
    }
}
