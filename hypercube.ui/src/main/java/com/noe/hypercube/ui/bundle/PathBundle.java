package com.noe.hypercube.ui.bundle;


import com.noe.hypercube.ui.domain.LocalFile;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.util.*;

public class PathBundle {

    private static final PathBundle instance = new PathBundle();
    private Map<String, DualHashBidiMap<String, String>> mappings;

    private PathBundle() {
        mappings = new HashMap<>();
//        mappings.put("test", new DualHashBidiMap(new HashMap<>()));
//        mappings.put("other", new DualHashBidiMap(new HashMap<>()));
        mappings.put("Dropbox", new DualHashBidiMap(new HashMap<>()));
//        mappings.get("test").put("C:\\Users", "/A/B/C");
//        mappings.get("test").put("C:\\Users", "/x");
        mappings.get("Dropbox").put("D:\\test", "/newtest");
    }

    public PathBundle(final Map<String, DualHashBidiMap<String, String>> mappings) {
        this.mappings = mappings;
    }

    public static String getFolder(final String account, final String localFolder) {
        return instance.mappings.get(account).get(localFolder);
    }

    public static String getLocalFolder(String account, String folder) {
        final DualHashBidiMap<String, String> accountMapping = instance.mappings.get(account);
        return accountMapping.getKey(folder.replaceAll("\\\\", "/"));
    }

    public static Map<String, String> getAllFolders(final String folder) {
        final Map<String, String> folders = new HashMap<>();
        final Set<String> accounts = instance.mappings.keySet();
        for (String account : accounts) {
            final String remoteFolderPath = getFolder(account, folder);
            if (remoteFolderPath != null) {
                folders.put(account, remoteFolderPath);
            }
        }
        return folders;
    }

    public static Set<String> getAccounts(LocalFile folder) {
        final Set<String> sharedAccounts = new HashSet<>();
        final Set<String> accounts = instance.mappings.keySet();
        for (String account : accounts) {
            if(isMapped(account, folder)) {
                sharedAccounts.add(account);
            }
        }
        return sharedAccounts;
    }

    private static boolean isMapped(final String account, final LocalFile folder) {
        final DualHashBidiMap<String, String> folderMap = instance.mappings.get(account);
        for (String mappedLocalFolder : folderMap.keySet()) {
            if(folder.getPath().toString().contains(mappedLocalFolder)) {
                return true;
            }
        }
        return false;
    }

    public static void add(final String account, final String localFolder, final String remoteFolder) {
        instance.mappings.get(account).put(localFolder, remoteFolder);
    }

    public static Collection<String> getAccounts() {
        return instance.mappings.keySet();
    }
}
