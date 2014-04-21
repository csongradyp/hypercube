package com.noe.hypercube;

import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MergingPersistenceUnitPostProcessor implements PersistenceUnitPostProcessor {
    private final Map<String, List<String>> puiClasses = new HashMap<String, List<String>>();

    public void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo pui) {
        List<String> classes = puiClasses.get(pui.getPersistenceUnitName());
        if (classes == null) {
            classes = new ArrayList<String>();
            puiClasses.put(pui.getPersistenceUnitName(), classes);
        }
        pui.getManagedClassNames().addAll(classes);
        classes.addAll(pui.getManagedClassNames());
    }
}
