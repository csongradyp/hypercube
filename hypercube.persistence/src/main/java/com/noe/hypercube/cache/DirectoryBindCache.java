package com.noe.hypercube.cache;

import com.noe.hypercube.domain.DirectoryBind;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DirectoryBindCache {

    private static final DirectoryBindCache INSTANCE = new DirectoryBindCache();

    private Map<String, String> remoteDirs;
    private Map<String, String> localDirs;
    private Collection<DirectoryBind> binds;

    private DirectoryBindCache() {
        remoteDirs = new HashMap<>(10);
        localDirs = new HashMap<>(10);
    }

    public static DirectoryBindCache getInstance() {
        return INSTANCE;
    }

    public void setBinds(Collection<DirectoryBind> binds) {
        this.binds = binds;
        bind(binds);
    }

    private void bind(Collection<DirectoryBind> binds) {
        for (DirectoryBind bind : binds) {
            bindDir(bind);
        }
    }

    private void bindDir(DirectoryBind bind) {
        String remoteDir = bind.getRemoteDir();
        if(notNullOrEmpty(remoteDir)) {
            remoteDirs.put(bind.getLocalDir(), remoteDir);
            localDirs.put(remoteDir, bind.getLocalDir());
        }
    }

    private boolean notNullOrEmpty(String remoteDir) {
        return remoteDir != null && !remoteDir.isEmpty();
    }

    public Map<String, String> getRemoteDirs() {
        return remoteDirs;
    }

    public Collection<DirectoryBind> getBinds() {
        return binds;
    }

    public Map<String, String> getLocalDirs() {
        return localDirs;
    }

}
