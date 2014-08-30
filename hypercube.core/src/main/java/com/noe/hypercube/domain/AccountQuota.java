package com.noe.hypercube.domain;

public class AccountQuota {

    private final Long totalSpace;
    private final Long usedSpace;

    public AccountQuota(final Long totalSpace, final Long usedSpace) {
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
