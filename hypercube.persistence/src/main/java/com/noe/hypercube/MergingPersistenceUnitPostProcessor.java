package com.noe.hypercube;

import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;

import java.util.*;

public class MergingPersistenceUnitPostProcessor implements PersistenceUnitPostProcessor {

    private final Map<String, Set<String>> puiClasses = new HashMap<>();

    @Override
    public void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo pui) {
        Set<String> classes = puiClasses.get(pui.getPersistenceUnitName());
        if (classes == null) {
            classes = new HashSet<>();
            puiClasses.put(pui.getPersistenceUnitName(), classes);
        }
        pui.getManagedClassNames().addAll(classes);
        classes.addAll(pui.getManagedClassNames());
    }
}
