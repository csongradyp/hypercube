package com.noe.hypercube.domain;

import java.util.Collection;

public interface Filter {

    boolean isWhiteList();

    boolean isBlackList();

    Collection<String> getFilters();

    void setFilters(Collection<String> filters);

}
