package com.noe.hypercube.domain.cloud;

import com.noe.hypercube.service.Account;

public class Cloud implements Account {

    private static final String name = "Cloud";

    public static String getName() {
        return name;
    }
}
