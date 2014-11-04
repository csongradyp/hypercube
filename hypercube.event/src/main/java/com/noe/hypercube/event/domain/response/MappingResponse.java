package com.noe.hypercube.event.domain.response;

import com.noe.hypercube.event.domain.MappingEvent;
import java.nio.file.Path;

public class MappingResponse extends MappingEvent {

    public MappingResponse(final Path localFolder) {
        super(localFolder);
    }
}
