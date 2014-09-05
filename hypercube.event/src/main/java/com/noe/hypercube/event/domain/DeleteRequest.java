package com.noe.hypercube.event.domain;

import java.nio.file.Path;

public class DeleteRequest implements IEvent{

    private Path path;
    private String id;

    public DeleteRequest(final Path path) {
        this.path = path;
    }

    public DeleteRequest(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Path getPath() {
        return path;
    }
}
