package com.noe.hypercube.domain;

import com.noe.hypercube.Action;

public interface IStreamEntry<DEPENDENCY> {

    boolean isDependent();

    DEPENDENCY getDependency();

    Action getAction();

    void setAction(Action action);

}
