package com.noe.hypercube.collector;

import com.noe.hypercube.domain.AccountBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import java.util.ArrayList;
import java.util.List;

public class AccountBoxCollector implements FactoryBean<List<AccountBox<?,?,?,?>>> {

    private static final Logger LOG = LoggerFactory.getLogger(AccountBoxCollector.class);

    private static final List<AccountBox<?,?,?,?>> list = new ArrayList<>();

    public AccountBoxCollector() {
    }

    public AccountBoxCollector(List<List<AccountBox<?,?,?,?>>> boxes) {
        for (List item : boxes) {
            if (item != null) {
                list.addAll(item);
                LOG.debug("{} was collected", item.getClass().getSimpleName());
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
