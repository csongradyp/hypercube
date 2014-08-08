package com.noe.hypercube.synchronization;

import java.nio.file.Path;

public class SynchronizationException extends Exception {

    private Path relatedFile;

    public SynchronizationException(String message) {
        super(message);
    }

    public SynchronizationException(String message, Throwable cause) {
        super(message, cause);
    }

    public Path getRelatedFile() {
        return relatedFile;
    }

    public void setRelatedFile(Path relatedFile) {
        this.relatedFile = relatedFile;
    }
}
