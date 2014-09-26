package com.noe.hypercube.event.domain;

import java.nio.file.Path;

public class DeleteRequest extends AccountActionEvent {

    private Path path;
    private String id;

    public DeleteRequest(final String account, final Path path) {
        super(account);
        this.path = path;
    }

    public DeleteRequest(final String account, final String id) {
        super(account);
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Path getPath() {
        return path;
    }
}
