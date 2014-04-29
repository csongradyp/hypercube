package com.noe.hypercube.observer.local;


import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.io.filefilter.FileFilterUtils.*;
import static org.apache.commons.io.filefilter.FileFilterUtils.trueFileFilter;

public final class FilterBuilder {

    private List<IOFileFilter> orFilters;
    private List<IOFileFilter> andFilters;
    private boolean isWhiteList;

    private FilterBuilder(final boolean isWhiteList) {
        this.isWhiteList = isWhiteList;
        orFilters = new ArrayList<>();
        andFilters = new ArrayList<>();
    }

    public static FilterBuilder aWhitelistFilter() {
        return new FilterBuilder(true);
    }

    public static FilterBuilder aBlacklistFilter() {
        return new FilterBuilder(false);
    }

    public FilterBuilder andFolderNameIs(String folderName) {
        andFilters.add(and(directoryFileFilter(), nameFileFilter(folderName)));
        return this;
    }

    public FilterBuilder andFolderNameContains(String folderRegex) {
        andFilters.add(and(directoryFileFilter(), new RegexFileFilter(folderRegex)));
        return this;
    }

    public FilterBuilder orFolderNameIs(String folderName) {
        orFilters.add(or(directoryFileFilter(), nameFileFilter(folderName)));
        return this;
    }

    public FilterBuilder orFolderNameContains(String folderRegex) {
        orFilters.add(or(directoryFileFilter(), new RegexFileFilter(folderRegex)));
        return this;
    }

    public FilterBuilder orFolderFilesContains(String fileRegex) {
        orFilters.add(makeFileOnly(new RegexFileFilter(fileRegex)));
        return this;
    }

    public IOFileFilter build() {
        IOFileFilter orMergedFilter = trueFileFilter();
        IOFileFilter andMergedFilter = trueFileFilter();
        if(!andFilters.isEmpty()) {
            andMergedFilter = and((IOFileFilter[]) andFilters.toArray());
        }
        if(!orFilters.isEmpty()) {
            orMergedFilter = or((IOFileFilter[]) orFilters.toArray());
        }
        IOFileFilter mergedFilter = and(andMergedFilter, orMergedFilter);
        if(isWhiteList) {
            return mergedFilter;
        }
        else {
            return notFileFilter(mergedFilter);
        }
    }

}
