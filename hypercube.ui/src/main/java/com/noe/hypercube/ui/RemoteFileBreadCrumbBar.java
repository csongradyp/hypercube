package com.noe.hypercube.ui;

public class RemoteFileBreadCrumbBar extends FileBreadCrumbBar {

   private String account;

    public RemoteFileBreadCrumbBar(String account) {
        super();
        this.account = account;
    }

    public String getAccount() {
        return account;
    }
}
