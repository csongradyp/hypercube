package com.noe.hypercube.event.domain.request;

import com.noe.hypercube.event.domain.MappingEvent;
import java.nio.file.Path;

public class MappingRequest extends MappingEvent {

    public MappingRequest(final Path localFolder) {
        super(localFolder);
    }
}
