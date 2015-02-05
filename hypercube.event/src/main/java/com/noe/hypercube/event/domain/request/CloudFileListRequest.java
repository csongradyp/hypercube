package com.noe.hypercube.event.domain.request;

import com.noe.hypercube.event.domain.AccountActionEvent;
import java.nio.file.Path;
import java.util.Collection;

public class CloudFileListRequest extends AccountActionEvent implements IFileListRequest {

    private Path previousFolder;
    private final Integer target;
    private final Collection<FileListRequest> requests;

    public CloudFileListRequest(final Integer target, final Collection<FileListRequest> requests, final Path previousFolder) {
        super("Cloud");
        this.target = target;
        this.requests = requests;
        this.previousFolder = previousFolder;
    }

    public Collection<FileListRequest> getRequests() {
        return requests;
    }

    @Override
    public Path getFolder() {
        return requests.iterator().next().getFolder();
    }

    public Path getPreviousFolder() {
        return previousFolder;
    }

    @Override
    public Boolean isCloud() {
        return true;
    }

    @Override
    public Integer getTarget() {
        return target;
    }
}
