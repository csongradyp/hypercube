package com.noe.hypercube.domain;

import com.noe.hypercube.persistence.domain.IEntity;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.Collection;

@Entity
public class FileFilter implements Filter, IEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private final boolean whiteList;
    @ElementCollection
    private Collection<String> filters;

    public FileFilter() {
        this(false);
    }

    public FileFilter(final boolean blackList) {
        this(blackList, new ArrayList<String>());
    }

    public FileFilter(Collection<String> filters) {
        this(false, filters);
    }

    public FileFilter(final boolean blackList, Collection<String> filters) {
        this.whiteList = blackList;
        this.filters = filters;
    }

    @Override
    public boolean isWhiteList() {
        return whiteList;
    }

    @Override
    public boolean isBlackList() {
        return !whiteList;
    }

    @Override
    public Collection<String> getFilters() {
        return filters;
    }

    @Override
    public void setFilters(Collection<String> filters) {
        this.filters = filters;
    }

    @Override
    public Long getId() {
        return id;
    }

}
