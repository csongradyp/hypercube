package com.noe.hypercube.collector;

import org.springframework.beans.factory.FactoryBean;

import java.util.List;
import java.util.Properties;

public class AccountPropertiesCollector implements FactoryBean<Properties> {

    private static final Properties list = new Properties();

    public AccountPropertiesCollector() {
    }

    public AccountPropertiesCollector(List<Properties> items) {
        for (Properties item : items) {
            if (item != null) {
                list.putAll(item);
            }
        }
    }

    @Override
    public Properties getObject() {
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
