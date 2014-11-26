package com.noe.hypercube.event.domain.request;

import com.noe.hypercube.event.domain.AccountActionEvent;
import java.nio.file.Path;

public class DeleteRequest extends AccountActionEvent {

    private final Path containingFolder;
    private Path path;
    private String id;

    public DeleteRequest(final String account, final Path path, final Path containingFolder) {
        super(account);
        this.path = path;
        this.containingFolder = containingFolder;
    }

    public DeleteRequest(final String account, final String id, final Path containingFolder) {
        super(account);
        this.id = id;
        this.containingFolder = containingFolder;
    }

    public String getId() {
        return id;
    }

    public Path getPath() {
        return path;
    }

    public Path getContainingFolder() {
        return containingFolder;
    }
}
