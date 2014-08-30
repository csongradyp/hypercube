package com.noe.hypercube.event.dto;

public class RemoteQuotaInfo {

    private final Long totalSpace;
    private final Long usedSpace;

    public RemoteQuotaInfo(final Long totalSpace, final Long usedSpace) {
        this.totalSpace = totalSpace;
        this.usedSpace = usedSpace;
    }

    public Long getTotalSpace() {
        return totalSpace;
    }

    public Long getUsedSpace() {
        return usedSpace;
    }
}
