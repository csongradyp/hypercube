package com.noe.hypercube.collector;

import com.noe.hypercube.domain.AccountBox;
import org.springframework.beans.factory.FactoryBean;

import java.util.ArrayList;
import java.util.List;

public class AccountBoxCollector implements FactoryBean<List<AccountBox<?,?,?>>> {

    private static final List<AccountBox<?,?,?>> list = new ArrayList<>();

    public AccountBoxCollector() {
    }

    public AccountBoxCollector(List<List<AccountBox<?,?,?>>> boxes) {
        for (List item : boxes) {
            if (item != null) {
                list.addAll(item);
            }
        }
    }

    @Override
    public List getObject() {
        return list;
    }

    @Override
    public Class<AccountBox> getObjectType() {
        return AccountBox.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
