package com.noe.hypercube.ui.bundle;


import com.noe.hypercube.event.EventBus;
import com.noe.hypercube.event.EventHandler;
import com.noe.hypercube.event.domain.response.MappingResponse;
import com.noe.hypercube.ui.domain.account.AccountInfo;
import com.noe.hypercube.ui.domain.file.LocalFile;
import java.nio.file.Path;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import net.engio.mbassy.listener.Handler;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.util.*;

public class PathBundle implements EventHandler<MappingResponse> {

    private static final PathBundle instance = new PathBundle();
    private Map<String, DualHashBidiMap<String, String>> mappings;

    private PathBundle() {
        mappings = new HashMap<>();
        final ObservableList<AccountInfo> accountInfos = AccountBundle.getAccounts();
        for (AccountInfo accountInfo : accountInfos) {
            if(!mappings.containsKey(accountInfo.getName())) {
                mappings.put(accountInfo.getName(), new DualHashBidiMap(new HashMap<>()));
            }
        }
        addListenerForAccountChanges();
        EventBus.subscribeToMappingResponse(this);
    }

    /**
     * Called by {@code com.noe.hypercube.bridge.PersistenceDataBridge} at program startup phase.
     * @param mappings Previously persisted mappings from database.
     */
    public static void setInitialMappings(final Map<String, DualHashBidiMap<String, String>> mappings) {
        instance.mappings.putAll(mappings);
    }

    private void addListenerForAccountChanges() {
        AccountBundle.getAccounts().addListener((ListChangeListener<AccountInfo>) change -> {
            while (change.next()) {
                final List<? extends AccountInfo> addedAccount = change.getAddedSubList();
                for (AccountInfo account : addedAccount) {
                    if(!mappings.containsKey(account.getName())) {
                        mappings.put(account.getName(), new DualHashBidiMap(new HashMap<>()));
                    }
                }
                final List<? extends AccountInfo> removedAccount = change.getRemoved();
                for (AccountInfo account : removedAccount) {
                    mappings.remove(account.getName());
                }
            }
        });
    }

    public PathBundle(final Map<String, DualHashBidiMap<String, String>> mappings) {
        this.mappings = mappings;
    }

    public static String getFolder(final String account, final String localFolder) {
        final DualHashBidiMap<String, String> accountMappings = instance.mappings.get(account);
        for (String mappedLocalFolder : accountMappings.keySet()) {
            if(localFolder.contains(mappedLocalFolder)) {
                final String subDirectories = localFolder.replace(mappedLocalFolder, "");
                return accountMappings.get(mappedLocalFolder) + subDirectories;
            }
        }
        return accountMappings.get(localFolder);
    }

    public static String getLocalFolder(final String account, final String folder) {
        final DualHashBidiMap<String, String> accountMapping = instance.mappings.get(account);
        return accountMapping.getKey(folder.replaceAll("\\\\", "/"));
    }

    public static Map<String, String> getAllRemoteFolders(final String folder) {
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

    public static Set<String> getLocalFolders() {
        final Set<String> localFolders = new HashSet<>();
        final Collection<DualHashBidiMap<String, String>> mappings = instance.mappings.values();
        for (DualHashBidiMap<String, String> mapping : mappings) {
            localFolders.addAll(mapping.keySet());
        }
        return localFolders;
    }

    public static Set<String> getLocalFolders(final String localRoot) {
        final Set<String> localFolders = new HashSet<>();
        final Collection<DualHashBidiMap<String, String>> mappings = instance.mappings.values();
        for (DualHashBidiMap<String, String> mapping : mappings) {
            final Set<String> folders = mapping.keySet();
            for (String folder : folders) {
                if(folder.contains(localRoot)) {
                    localFolders.add(folder);
                }
            }
        }
        return localFolders;
    }

    public static Set<String> getLocalFoldersByRemote(final String remoteFolder) {
        final Set<String> localFolders = new HashSet<>();
        final Set<String> accounts = instance.mappings.keySet();
        for (String account : accounts) {
            final Set<Map.Entry<String, String>> mappings = instance.mappings.get(account).entrySet();
            for (Map.Entry<String, String> mapping : mappings) {
                if(mapping.getValue().equals(remoteFolder)) {
                    localFolders.add(mapping.getKey());
                }
            }
        }
        return localFolders;
    }

    public static Set<String> getRemoteFolders(final String account) {
        final Set<String> remoteFolders = new HashSet<>();
        final DualHashBidiMap<String, String> mappings = instance.mappings.get(account);
        remoteFolders.addAll(mappings.values());
        return remoteFolders;
    }

    public static Set<String> getAccounts(final LocalFile folder) {
        final Set<String> sharedAccounts = new HashSet<>();
        final Set<String> accounts = instance.mappings.keySet();
        for (String account : accounts) {
            if (isMapped(account, folder)) {
                sharedAccounts.add(account);
            }
        }
        return sharedAccounts;
    }

    private static boolean isMapped(final String account, final LocalFile folder) {
        final DualHashBidiMap<String, String> folderMap = instance.mappings.get(account);
        for (String mappedLocalFolder : folderMap.keySet()) {
            if (folder.getPath().toString().contains(mappedLocalFolder)) {
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

    @Override
    @Handler(rejectSubtypes = true)
    public void onEvent(final MappingResponse event) {
        final Map<String, Path> remoteFolders = event.getRemoteFolders();
        for (Map.Entry<String, Path> remoteMapping : remoteFolders.entrySet()) {
            add(remoteMapping.getKey(), event.getLocalFolder().toString(), remoteMapping.getValue().toString());
        }
    }
}
