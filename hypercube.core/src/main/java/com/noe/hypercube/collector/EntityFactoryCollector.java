package com.noe.hypercube.collector;

import org.springframework.beans.factory.FactoryBean;

import java.util.ArrayList;
import java.util.List;

public class EntityFactoryCollector implements FactoryBean<List> {

    private static final List list = new ArrayList();

    public EntityFactoryCollector() {
    }

    public EntityFactoryCollector(List<List> items) {
        for (List item : items) {
            if (item != null) {
                list.addAll(item);
            }
        }
    }

    @Override
    public List getObject() throws Exception {
        return list;
    }

    @Override
    public Class getObjectType() {
        return list.getClass();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
