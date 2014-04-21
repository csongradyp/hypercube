package com.noe.hypercube.mapping.collector;

import java.nio.file.Path;
import java.util.Collection;

public interface Collector<E> {

    Collection<E> collect(Path filePath, String fileName, Collection<E> mappings);
}
