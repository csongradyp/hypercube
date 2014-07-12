package com.noe.hypercube.domain;

import java.util.ArrayList;
import java.util.Collection;

public class TestFilter implements Filter {

    private Collection<String> filters;

    public TestFilter(Collection<String> filters) {
        this.filters = filters;
    }

    public TestFilter() {
        this(new ArrayList<>());
    }

    @Override
    public boolean isWhiteList() {
        return true;
    }

    @Override
    public boolean isBlackList() {
        return false;
    }

    @Override
    public Collection<String> getFilters() {
        return filters;
    }

    @Override
    public void setFilters(Collection<String> filters) {
        this.filters = filters;
    }
}
