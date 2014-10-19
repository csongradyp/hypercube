package com.noe.hypercube.event.domain;

import java.nio.file.Path;

public class MappingRequest extends MappingEvent {

    public MappingRequest(final Path localFolder) {
        super(localFolder);
    }
}
