package com.noe.hypercube.observer;


import com.noe.hypercube.controller.IPersistenceController;
import com.noe.hypercube.domain.Filter;
import com.noe.hypercube.domain.MappingEntity;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public class LocalFileMonitor {

    private static final Logger LOG = Logger.getLogger(LocalFileObserver.class);

    @Inject
    private FileAlterationMonitor fileMonitor;
    @Inject
    private List<FileAlterationObserver> observers;
    @Inject
    private IPersistenceController persistenceController;
    @Inject
    private LocalFileListener fileListener;


    public LocalFileMonitor(List<FileAlterationObserver> observers) {
        this.observers = observers;
        for (FileAlterationObserver observer : observers) {
            fileMonitor.addObserver(observer);
        }
    }

    private void createObservers() {
        Collection<MappingEntity> allMappings = persistenceController.getAllMappings();
        for (MappingEntity mapping : allMappings) {
            Path localDir = mapping.getLocalDir();
//            createFilters(mapping);
            FileAlterationObserver observer = new FileAlterationObserver(localDir.toString());
            observer.addListener(fileListener);
            fileMonitor.addObserver(observer);
        }
    }

    private IOFileFilter createFilters(MappingEntity mapping) {
        Filter filter = mapping.getFilter();
        Collection<String> filters = filter.getFilters();
        for (String filterPattern : filters) {
            RegexFileFilter regexFileFilter = new RegexFileFilter(filterPattern);
        }
        return null;
    }

    public void start() {
        try {
            fileMonitor.start();
        } catch (Exception e) {
            LOG.error("Failed to start File monitor", e);
        }
    }

    public List<FileAlterationObserver> getObservers() {
        return observers;
    }
}
