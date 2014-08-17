package com.noe.hypercube.ui.bundle;

import org.apache.commons.collections.DualHashBidiMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PathBundle {

    private Map<String, Map<String, String>> mappings;

    public PathBundle() {
        mappings = new HashMap<>();
        mappings.put("test", new DualHashBidiMap(new HashMap<>()));
        mappings.put("other", new DualHashBidiMap(new HashMap<>()));
        mappings.put("Dropbox", new DualHashBidiMap(new HashMap<>()));
        add("test", "C:\\Users", "/A/B/C");
        add("other", "C:\\Users", "/x/y");
        add("Dropbox", "D:\\test", "/newtest");
    }

    public PathBundle(Map<String, Map<String, String>> mappings) {
        this.mappings = mappings;
    }

    public String getFolder(final String account, final String localFolder) {
        return mappings.get(account).get(localFolder);
    }

    public Map<String, String> getAllFolders(final String folder) {
        final Map<String, String> folders = new HashMap<>();
        for (String account : mappings.keySet()) {
            final String remoteFolderPath = getFolder(account, folder);
            if (remoteFolderPath != null) {
                folders.put(account, remoteFolderPath);
            }
        }
        return folders;
    }

    public void add(final String account, final String localFolder, final String remoteFolder) {
        mappings.get(account).put(localFolder, remoteFolder);
    }

    public Collection<String> getAccounts() {
        return mappings.keySet();
    }

}
