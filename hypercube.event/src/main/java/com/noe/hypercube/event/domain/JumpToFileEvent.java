package com.noe.hypercube.event.domain;

import java.nio.file.Path;

public class JumpToFileEvent implements IEvent {

    private final Path filePath;

    public JumpToFileEvent(final Path filePath) {
        this.filePath = filePath;
    }

    public Path getFilePath() {
        return filePath;
    }
}
