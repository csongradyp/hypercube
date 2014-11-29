package com.noe.hypercube.event.domain;

import java.nio.file.Path;

public class JumpToFileEvent implements IEvent {

    private Boolean remote;
    private final String account;
    private final Path filePath;

    public JumpToFileEvent(final String account, final Path filePath) {
        this.account = account;
        this.filePath = filePath;
    }

    public Path getFilePath() {
        return filePath;
    }
}
