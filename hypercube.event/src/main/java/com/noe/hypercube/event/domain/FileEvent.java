package com.noe.hypercube.event.domain;

import java.nio.file.Path;

public class FileEvent implements IEvent {

    private FileEventType eventType;
    private Path path;
}
