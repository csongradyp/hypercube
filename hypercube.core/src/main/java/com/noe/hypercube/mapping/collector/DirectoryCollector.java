package com.noe.hypercube.mapping.collector;

import com.noe.hypercube.domain.Filter;
import com.noe.hypercube.persistence.domain.MappingEntity;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class DirectoryCollector implements Collector<MappingEntity> {

    protected abstract Path getDirectoryToMatch(MappingEntity mapping);

    @Override
    public Collection<MappingEntity> collect(final Path filePath, final String fileName, final Collection<MappingEntity> mappings) {
        List<MappingEntity> matchedDirs = new ArrayList<>(5);
        for (MappingEntity mapping : mappings) {
            Path directoryToMatch = getDirectoryToMatch(mapping);
            if (directoryToMatch.equals(filePath) && isMatchingFilter(fileName, mapping)) {
                matchedDirs.add(mapping);
            }
        }
        return matchedDirs;
    }

    private boolean isMatchingFilter(final String fileName, MappingEntity mapping) {
        Filter fileFilter = mapping.getFilter();
        if (fileFilter != null) {
            Collection<String> filters = fileFilter.getFilters();
            for (String filter : filters) {
                if (fileName.matches(filter) && fileFilter.isBlackList()) {
                    return false;
                }
            }
        }
        return true;
    }
}
