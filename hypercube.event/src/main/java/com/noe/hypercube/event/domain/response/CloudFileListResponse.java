package com.noe.hypercube.event.domain.response;

import com.noe.hypercube.domain.ServerEntry;
import com.noe.hypercube.event.dto.RemoteQuotaInfo;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public class CloudFileListResponse extends FileListResponse {

    public static final String CLOUD = "Cloud";
    private final Collection<String> accounts;

    public CloudFileListResponse(final Integer target, final Collection<String> accounts, final Path previousFolder, final Path folder, final List<ServerEntry> fileList, final RemoteQuotaInfo quotaInfo) {
        super(target, CLOUD, previousFolder, folder, fileList, quotaInfo);
        this.accounts = accounts;
    }

    public Collection<String> getAccounts() {
        return accounts;
    }
}
